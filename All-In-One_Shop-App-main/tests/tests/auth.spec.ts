import { test, expect } from '@playwright/test';

const ADMIN_EMAIL = 'admin@allinone.com';
const ADMIN_PASSWORD = 'admin123';

test.describe('Authentication', () => {
  test('login page loads with all required elements', async ({ page }) => {
    await page.goto('/login');
    await expect(page.locator('input[type="email"]')).toBeVisible();
    await expect(page.locator('input[type="password"]')).toBeVisible();
    await expect(page.locator('button[type="submit"]')).toBeVisible();
  });

  test('login with valid credentials redirects to /admin', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="email"]', ADMIN_EMAIL);
    await page.fill('input[type="password"]', ADMIN_PASSWORD);
    await page.click('button[type="submit"]');
    await page.waitForURL('/admin', { timeout: 15_000 });
    await expect(page).toHaveURL('/admin');
  });

  test('login with wrong password stays on /login', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="email"]', ADMIN_EMAIL);
    await page.fill('input[type="password"]', 'wrongpassword');
    await page.click('button[type="submit"]');
    await page.waitForTimeout(2500);
    await expect(page).toHaveURL('/login');
  });

  test('login with unknown email stays on /login', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="email"]', 'nobody@example.com');
    await page.fill('input[type="password"]', 'somepassword');
    await page.click('button[type="submit"]');
    await page.waitForTimeout(2500);
    await expect(page).toHaveURL('/login');
  });

  test('empty form is blocked by HTML5 required validation', async ({ page }) => {
    await page.goto('/login');
    await page.click('button[type="submit"]');
    await expect(page).toHaveURL('/login');
  });

  test('session persists — /admin stays accessible without re-login', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="email"]', ADMIN_EMAIL);
    await page.fill('input[type="password"]', ADMIN_PASSWORD);
    await page.click('button[type="submit"]');
    await page.waitForURL('/admin', { timeout: 15_000 });

    await page.goto('/admin');
    await expect(page).toHaveURL('/admin');
  });
});
