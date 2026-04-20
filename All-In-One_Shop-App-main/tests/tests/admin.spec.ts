import { test, expect, Page } from '@playwright/test';

const ADMIN_EMAIL = 'admin@allinone.com';
const ADMIN_PASSWORD = 'admin123';

async function loginAsAdmin(page: Page) {
  await page.goto('/login');
  await page.fill('input[type="email"]', ADMIN_EMAIL);
  await page.fill('input[type="password"]', ADMIN_PASSWORD);
  await page.click('button[type="submit"]');
  await page.waitForURL('/admin', { timeout: 15_000 });
}

test.describe('Admin Panel — Products CRUD', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
  });

  test('admin dashboard loads', async ({ page }) => {
    await expect(page).toHaveURL('/admin');
    await expect(page.locator('body')).toBeVisible();
  });

  test('products tab shows a table with seeded data', async ({ page }) => {
    const productsTab = page
      .locator('button, a, [role="tab"]')
      .filter({ hasText: /products/i })
      .first();
    if (await productsTab.isVisible()) await productsTab.click();
    await page.waitForTimeout(1500);
    await expect(page.locator('table').first()).toBeVisible({ timeout: 10_000 });
  });

  test('clicking Add Product opens the modal', async ({ page }) => {
    const productsTab = page
      .locator('button, a, [role="tab"]')
      .filter({ hasText: /products/i })
      .first();
    if (await productsTab.isVisible()) await productsTab.click();
    await page.waitForTimeout(1000);

    const addBtn = page.locator('button').filter({ hasText: /add product/i }).first();
    await expect(addBtn).toBeVisible({ timeout: 8_000 });
    await addBtn.click();

    await expect(
      page.locator('h2').filter({ hasText: /add new product/i })
    ).toBeVisible({ timeout: 5_000 });
  });

  test('submitting a filled Add Product form closes the modal', async ({ page }) => {
    const productsTab = page
      .locator('button, a, [role="tab"]')
      .filter({ hasText: /products/i })
      .first();
    if (await productsTab.isVisible()) await productsTab.click();
    await page.waitForTimeout(1000);

    await page.locator('button').filter({ hasText: /add product/i }).click();
    await page.waitForTimeout(500);

    // Fill name
    await page.locator('input[type="text"]').first().fill('Playwright Test Product');

    // Select first non-placeholder option in each select
    const selects = page.locator('select');
    const selectCount = await selects.count();
    for (let i = 0; i < selectCount; i++) {
      await selects.nth(i).selectOption({ index: 1 });
    }

    await page.locator('button[type="submit"]').click();
    await page.waitForTimeout(2000);

    // Modal should have closed on success
    await expect(
      page.locator('h2').filter({ hasText: /add new product/i })
    ).not.toBeVisible({ timeout: 5_000 });
  });

  test('Cancel button closes the Add Product modal without saving', async ({ page }) => {
    const productsTab = page
      .locator('button, a, [role="tab"]')
      .filter({ hasText: /products/i })
      .first();
    if (await productsTab.isVisible()) await productsTab.click();
    await page.waitForTimeout(1000);

    await page.locator('button').filter({ hasText: /add product/i }).click();
    await page.waitForTimeout(500);

    await expect(
      page.locator('h2').filter({ hasText: /add new product/i })
    ).toBeVisible();

    await page.locator('button').filter({ hasText: /cancel/i }).click();

    await expect(
      page.locator('h2').filter({ hasText: /add new product/i })
    ).not.toBeVisible();
  });

  test('clicking the edit button on a row opens the Edit Product modal', async ({ page }) => {
    const productsTab = page
      .locator('button, a, [role="tab"]')
      .filter({ hasText: /products/i })
      .first();
    if (await productsTab.isVisible()) await productsTab.click();
    await page.waitForTimeout(1500);

    // Click first SVG button in the table (edit icon)
    const editBtn = page
      .locator('table button')
      .filter({ has: page.locator('svg') })
      .first();

    if (await editBtn.isVisible()) {
      await editBtn.click();
      await page.waitForTimeout(500);
      await expect(
        page.locator('h2').filter({ hasText: /edit product/i })
      ).toBeVisible({ timeout: 5_000 });
    }
  });
});
