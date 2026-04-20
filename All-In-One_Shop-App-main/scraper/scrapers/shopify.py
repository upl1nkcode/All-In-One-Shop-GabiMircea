"""
Generic Shopify store scraper.
Works with any Shopify store by hitting their public /products.json endpoint.
Returns real products with real images and real links.
"""
import random
import logging
import requests
from .base import BaseScraper, ScrapedProduct

logger = logging.getLogger(__name__)

# Category mapping: Shopify product_type -> our category
CATEGORY_MAP = {
    # Tops
    "t-shirts": "T-Shirts", "tees": "T-Shirts", "tee": "T-Shirts",
    "t-shirt": "T-Shirts", "tees & tops": "T-Shirts", "tops": "T-Shirts",
    "graphic tees": "T-Shirts", "short sleeve": "T-Shirts",
    # Hoodies
    "hoodies": "Hoodies", "hoodie": "Hoodies", "sweatshirts": "Hoodies",
    "pullover": "Hoodies", "crewneck sweaters": "Hoodies",
    "crewnecks": "Hoodies", "fleece": "Hoodies",
    # Jackets
    "jackets": "Jackets", "jacket": "Jackets", "outerwear": "Jackets",
    "coats": "Jackets", "bomber": "Jackets", "bombers": "Jackets",
    "puffer": "Jackets", "windbreaker": "Jackets",
    # Pants
    "pants": "Pants", "joggers": "Pants", "trousers": "Pants",
    "sweatpants": "Pants", "track pants": "Pants", "cargo": "Pants",
    "leggings": "Pants", "shorts": "Pants",
    # Jeans
    "jeans": "Jeans", "denim": "Jeans",
    # Sneakers
    "sneakers": "Sneakers", "shoes": "Sneakers", "footwear": "Sneakers",
    "runners": "Sneakers", "trainers": "Sneakers",
    "low tops": "Sneakers", "high tops": "Sneakers",
    "running shoes": "Sneakers", "lifestyle shoes": "Sneakers",
}


def map_category(product_type: str) -> str | None:
    """Map a Shopify product_type to our category system."""
    if not product_type:
        return None
    pt = product_type.strip().lower()
    if pt in CATEGORY_MAP:
        return CATEGORY_MAP[pt]
    # Partial match
    for key, val in CATEGORY_MAP.items():
        if key in pt or pt in key:
            return val
    return None


def extract_sizes(variants: list) -> list[str]:
    """Extract unique size options from Shopify variants."""
    sizes = set()
    for v in variants:
        for opt in [v.get("option1"), v.get("option2"), v.get("option3")]:
            if opt and opt.upper() in (
                "XS", "S", "M", "L", "XL", "XXL", "2XL", "3XL",
                "28", "29", "30", "31", "32", "33", "34", "36", "38",
                "39", "40", "41", "42", "43", "44", "45", "46",
                "6", "7", "8", "9", "10", "11", "12", "13", "14",
            ):
                sizes.add(opt)
    return sorted(sizes)


def extract_colors(variants: list) -> list[str]:
    """Extract color names from variants."""
    colors = set()
    for v in variants:
        for opt in [v.get("option1"), v.get("option2"), v.get("option3")]:
            if opt and opt.upper() not in (
                "XS", "S", "M", "L", "XL", "XXL", "2XL", "3XL",
                "28", "29", "30", "31", "32", "33", "34", "36", "38",
                "39", "40", "41", "42", "43", "44", "45", "46",
                "6", "7", "8", "9", "10", "11", "12", "13", "14",
                "OS", "ONE SIZE", "N/A", "DEFAULT TITLE",
            ):
                if len(opt) < 30:  # Avoid long strings
                    colors.add(opt)
    return sorted(colors)[:5]


class ShopifyScraper(BaseScraper):
    """Scraper for any Shopify-powered store."""

    def __init__(self, name: str, domain: str, currency: str = "USD",
                 collections: list[str] | None = None):
        self._name = name
        self._domain = domain
        self._currency = currency
        self._collections = collections or ["all"]
        self._session = requests.Session()
        self._session.headers.update({
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
            "Accept": "application/json",
        })

    @property
    def store_name(self) -> str:
        return self._name

    @property
    def base_url(self) -> str:
        return f"https://{self._domain}"

    def scrape(self) -> list[ScrapedProduct]:
        """Scrape real products from the Shopify store."""
        logger.info(f"Scraping {self.store_name} ({self._domain})...")
        products = []

        for collection in self._collections:
            try:
                page_products = self._scrape_collection(collection)
                products.extend(page_products)
            except Exception as e:
                logger.error(f"Failed to scrape collection {collection} from {self.store_name}: {e}")

        logger.info(f"Scraped {len(products)} products from {self.store_name}")
        return products

    def _scrape_collection(self, collection: str) -> list[ScrapedProduct]:
        """Scrape a single collection, picking a random page for variety."""
        results = []

        # Pick a random page for variety on each run
        page = random.randint(1, 8)
        limit = 30

        url = f"https://{self._domain}/collections/{collection}/products.json"
        params = {"limit": limit, "page": page}

        try:
            resp = self._session.get(url, params=params, timeout=15)
            resp.raise_for_status()
            data = resp.json()
        except Exception as e:
            logger.warning(f"Page {page} failed for {self.store_name}/{collection}, trying page 1: {e}")
            # Fallback to page 1
            params["page"] = 1
            resp = self._session.get(url, params=params, timeout=15)
            resp.raise_for_status()
            data = resp.json()

        raw_products = data.get("products", [])
        if not raw_products:
            logger.info(f"No products on page {page} for {self.store_name}/{collection}")
            return []

        for raw in raw_products:
            try:
                product = self._parse_product(raw)
                if product:
                    results.append(product)
            except Exception as e:
                logger.debug(f"Skipping product: {e}")

        return results

    def _parse_product(self, raw: dict) -> ScrapedProduct | None:
        """Parse a raw Shopify product JSON into a ScrapedProduct."""
        title = raw.get("title", "").strip()
        if not title:
            return None

        product_type = raw.get("product_type", "")
        category = map_category(product_type)
        if not category:
            return None  # Skip items we can't categorize (accessories, etc.)

        vendor = raw.get("vendor", self._name)
        handle = raw.get("handle", "")
        body_html = raw.get("body_html", "")
        # Strip HTML tags for description
        import re
        description = re.sub(r"<[^>]+>", " ", body_html or "").strip()
        description = " ".join(description.split())[:500]  # Clean whitespace, limit length

        # Get variants for price info
        variants = raw.get("variants", [])
        if not variants:
            return None

        # Find cheapest available variant
        available_variants = [v for v in variants if v.get("available", False)]
        if not available_variants:
            available_variants = variants  # Use all if none available

        cheapest = min(available_variants, key=lambda v: float(v.get("price", "9999")))
        price = float(cheapest.get("price", 0))
        compare_price = cheapest.get("compare_at_price")
        original_price = float(compare_price) if compare_price else None

        if price <= 0:
            return None

        # Images
        images = raw.get("images", [])
        image_url = images[0]["src"] if images else ""
        additional_images = [img["src"] for img in images[1:4]] if len(images) > 1 else []

        if not image_url:
            return None

        # Sizes and colors
        sizes = extract_sizes(variants)
        colors = extract_colors(variants)

        # Product URL
        product_url = f"https://{self._domain}/products/{handle}"

        # Check stock
        in_stock = any(v.get("available", False) for v in variants)

        return ScrapedProduct(
            name=title,
            description=description if description else f"{vendor} {title}",
            brand=vendor,
            category=category,
            image_url=image_url,
            price=price,
            original_price=original_price,
            currency=self._currency,
            product_url=product_url,
            store_name=self._name,
            in_stock=in_stock,
            sizes=sizes,
            colors=colors,
            additional_images=additional_images,
        )
