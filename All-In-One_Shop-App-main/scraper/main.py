"""
AllInOne Shop — Python Scraper
Collects real product data from online stores and pushes to the backend API.
Supports on-demand scraping via HTTP trigger and scheduled runs.
"""
import os
import time
import threading
import logging
from http.server import HTTPServer, BaseHTTPRequestHandler
import json

import schedule

from scrapers.shopify import ShopifyScraper
from utils.api_client import ApiClient

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)
logger = logging.getLogger(__name__)

API_URL = os.getenv("SCRAPER_API_URL", "http://localhost:8080/api")
API_KEY = os.getenv("SCRAPER_API_KEY", "")
TRIGGER_PORT = int(os.getenv("SCRAPER_TRIGGER_PORT", "9090"))

# ── Store configurations ──────────────────────────────────────────
# Each entry creates a real scraper that hits the store's Shopify API.
STORE_CONFIGS = [
    {
        "name": "Kith",
        "domain": "kith.com",
        "currency": "USD",
        "collections": ["all"],
    },
    {
        "name": "MNML",
        "domain": "mnml.la",
        "currency": "USD",
        "collections": ["all"],
    },
    {
        "name": "CultureKings",
        "domain": "www.culturekings.com.au",
        "currency": "AUD",
        "collections": ["all"],
    },
    {
        "name": "Asphaltgold",
        "domain": "www.asphaltgold.com",
        "currency": "EUR",
        "collections": ["all"],
    },
]


def build_scrapers():
    """Build scraper instances from config."""
    scrapers = []
    for cfg in STORE_CONFIGS:
        scrapers.append(ShopifyScraper(
            name=cfg["name"],
            domain=cfg["domain"],
            currency=cfg.get("currency", "USD"),
            collections=cfg.get("collections", ["all"]),
        ))
    return scrapers


def wait_for_backend(url: str, timeout: int = 120):
    """Block until the backend health endpoint responds (or timeout)."""
    import requests
    health_url = url.rstrip("/").rsplit("/api", 1)[0] + "/actuator/health"
    # Also try the base /api as a fallback
    fallback_url = url.rstrip("/") + "/products?size=1"
    deadline = time.time() + timeout
    while time.time() < deadline:
        for check_url in [health_url, fallback_url]:
            try:
                r = requests.get(check_url, timeout=5)
                if r.status_code < 500:
                    logger.info(f"Backend is ready (responded at {check_url})")
                    return True
            except Exception:
                pass
        logger.info("Waiting for backend to become ready...")
        time.sleep(5)
    logger.warning("Backend did not become ready within timeout")
    return False


def run_scrapers():
    """Run all configured scrapers and push results to the backend."""
    logger.info("=== Starting scrape cycle ===")

    # Wait for backend before pushing
    wait_for_backend(API_URL)

    client = ApiClient(API_URL, API_KEY)
    scrapers = build_scrapers()

    total_found = 0
    total_pushed = 0

    for scraper in scrapers:
        try:
            logger.info(f"Running {scraper.store_name} scraper...")
            products = scraper.scrape()
            total_found += len(products)
            logger.info(f"Found {len(products)} products from {scraper.store_name}")

            for product in products:
                try:
                    client.upsert_product(product)
                    total_pushed += 1
                except Exception as e:
                    logger.error(f"Failed to push product '{product.name}': {e}")

            logger.info(f"Completed {scraper.store_name} scraper")
        except Exception as e:
            logger.error(f"Scraper {scraper.store_name} failed: {e}")

    logger.info(f"=== Scrape cycle complete: {total_found} found, {total_pushed} pushed ===")
    return {"totalFound": total_found, "totalPushed": total_pushed}


# ── HTTP trigger server ───────────────────────────────────────────
class TriggerHandler(BaseHTTPRequestHandler):
    """Simple HTTP handler to trigger scraping on demand."""

    def do_POST(self):
        if self.path == "/scrape":
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(json.dumps({"status": "started"}).encode())
            # Run in a thread so the response returns immediately
            threading.Thread(target=run_scrapers, daemon=True).start()
        else:
            self.send_response(404)
            self.end_headers()

    def do_GET(self):
        if self.path == "/health":
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(json.dumps({"status": "ok"}).encode())
        else:
            self.send_response(404)
            self.end_headers()

    def log_message(self, format, *args):
        logger.debug(f"HTTP: {format % args}")


def start_trigger_server():
    """Start the HTTP trigger server in a background thread."""
    server = HTTPServer(("0.0.0.0", TRIGGER_PORT), TriggerHandler)
    logger.info(f"Trigger server listening on port {TRIGGER_PORT}")
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()


# ── Main ──────────────────────────────────────────────────────────
if __name__ == "__main__":
    # Start HTTP trigger server for on-demand scraping
    start_trigger_server()

    # Run once immediately on startup
    run_scrapers()

    # Schedule to run every 6 hours
    schedule.every(6).hours.do(run_scrapers)

    logger.info("Scraper scheduler started. Running every 6 hours.")
    while True:
        schedule.run_pending()
        time.sleep(60)
