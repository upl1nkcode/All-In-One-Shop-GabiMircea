"""
Zalando scraper — placeholder.
Zalando's API requires browser-level access (blocks plain HTTP).
This scraper is kept as a stub for future Playwright-based implementation.
"""
import logging
from .base import BaseScraper, ScrapedProduct

logger = logging.getLogger(__name__)


class ZalandoScraper(BaseScraper):
    """Scraper for zalando.com (placeholder — requires Playwright for real scraping)."""

    @property
    def store_name(self) -> str:
        return "Zalando"

    @property
    def base_url(self) -> str:
        return "https://www.zalando.com"

    def scrape(self) -> list[ScrapedProduct]:
        logger.info("Zalando scraper is a placeholder — skipping (requires Playwright).")
        return []
