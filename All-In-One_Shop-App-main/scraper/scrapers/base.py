"""Base scraper class — all store scrapers inherit from this."""
from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from typing import Optional


@dataclass
class ScrapedProduct:
    """Normalized product data from any store."""
    name: str
    description: str
    brand: str
    category: str
    image_url: str
    price: float
    original_price: Optional[float]
    currency: str
    product_url: str
    store_name: str
    in_stock: bool = True
    sizes: list[str] = field(default_factory=list)
    colors: list[str] = field(default_factory=list)
    additional_images: list[str] = field(default_factory=list)


class BaseScraper(ABC):
    """Abstract base class for all store scrapers."""

    @property
    @abstractmethod
    def store_name(self) -> str:
        """Name of the store being scraped."""
        ...

    @property
    @abstractmethod
    def base_url(self) -> str:
        """Base URL of the store."""
        ...

    @abstractmethod
    def scrape(self) -> list[ScrapedProduct]:
        """
        Scrape products from the store.
        Returns a list of normalized ScrapedProduct objects.
        """
        ...
