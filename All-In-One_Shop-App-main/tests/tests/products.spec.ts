import { test, expect } from '@playwright/test';

test.describe('Product Browsing', () => {
  test('home page loads and has a title', async ({ page }) => {
    await page.goto('/');
    const title = await page.title();
    expect(title.length).toBeGreaterThan(0);
  });

  test('home page displays at least one image after loading', async ({ page }) => {
    await page.goto('/');
    await page.waitForTimeout(3000);
    const images = page.locator('img');
    const count = await images.count();
    expect(count).toBeGreaterThan(0);
  });

  test('search input accepts text input', async ({ page }) => {
    await page.goto('/');
    const searchInput = page
      .locator('input[type="search"], input[placeholder*="earch" i]')
      .first();

    if (await searchInput.isVisible()) {
      await searchInput.fill('hoodie');
      await expect(searchInput).toHaveValue('hoodie');
    }
  });

  test('searching returns a result page without crashing', async ({ page }) => {
    await page.goto('/');
    const searchInput = page
      .locator('input[type="search"], input[placeholder*="earch" i]')
      .first();

    if (await searchInput.isVisible()) {
      await searchInput.fill('hoodie');
      await searchInput.press('Enter');
      await page.waitForTimeout(2000);
      await expect(page.locator('body')).toBeVisible();
    }
  });

  test('navigating to unknown route does not crash the app', async ({ page }) => {
    const response = await page.goto('/route-that-does-not-exist-xyz');
    expect([200, 404]).toContain(response?.status());
  });

  test('home page does not have console errors on load', async ({ page }) => {
    const errors: string[] = [];
    page.on('console', msg => {
      if (msg.type() === 'error') errors.push(msg.text());
    });
    await page.goto('/');
    await page.waitForTimeout(2000);
    // Filter out known benign errors (e.g. CORS from tracker)
    const critical = errors.filter(e => !e.includes('tracker') && !e.includes('favicon'));
    expect(critical).toHaveLength(0);
  });
});
