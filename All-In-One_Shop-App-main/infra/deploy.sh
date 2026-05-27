#!/usr/bin/env bash
# ============================================================
# All-In-One Shop — Oracle Cloud VM bootstrap + deploy script
#
# Run once on a fresh Ubuntu 22.04 ARM VM after SSH-ing in:
#   chmod +x deploy.sh && ./deploy.sh
#
# What it does:
#   1. Installs Docker, docker-compose, Caddy, git
#   2. Opens firewall ports 22/80/443
#   3. Clones the repo (or pulls latest if already present)
#   4. Guides you to create the .env file with real secrets
#   5. Starts all backend services with docker-compose.prod.yml
#   6. Tells you the final Caddy step
# ============================================================
set -euo pipefail

REPO_URL="https://github.com/upl1nkcode/All-In-One-Shop-GabiMircea.git"
BRANCH="fresh-ui"
APP_DIR="$HOME/allinone-shop"
INFRA_DIR="$APP_DIR/All-In-One_Shop-App-main/infra"

# ── 1. System packages ───────────────────────────────────────
echo ""
echo "==> [1/6] Updating system packages..."
sudo apt-get update -y
sudo apt-get install -y curl git apt-transport-https

# ── 2. Docker ────────────────────────────────────────────────
echo ""
echo "==> [2/6] Installing Docker..."
if ! command -v docker &>/dev/null; then
  curl -fsSL https://get.docker.com | sudo sh
  sudo usermod -aG docker "$USER"
  echo "     NOTE: Docker group added. Using 'sudo docker' for this session."
fi

if ! command -v docker-compose &>/dev/null; then
  sudo apt-get install -y docker-compose
fi

# ── 3. Caddy ─────────────────────────────────────────────────
echo ""
echo "==> [3/6] Installing Caddy..."
if ! command -v caddy &>/dev/null; then
  sudo apt-get install -y debian-keyring debian-archive-keyring
  curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/gpg.key' \
    | sudo gpg --dearmor -o /usr/share/keyrings/caddy-stable-archive-keyring.gpg
  curl -1sLf 'https://dl.cloudsmith.io/public/caddy/stable/debian.deb.txt' \
    | sudo tee /etc/apt/sources.list.d/caddy-stable.list
  sudo apt-get update -y
  sudo apt-get install -y caddy
fi

# ── 4. Firewall ──────────────────────────────────────────────
echo ""
echo "==> [4/6] Opening firewall ports 22, 80, 443..."
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw --force enable

# ── 5. Clone / pull repo ─────────────────────────────────────
echo ""
echo "==> [5/6] Fetching repository (branch: $BRANCH)..."
if [ -d "$APP_DIR/.git" ]; then
  git -C "$APP_DIR" fetch origin
  git -C "$APP_DIR" checkout "$BRANCH"
  git -C "$APP_DIR" pull
else
  git clone --branch "$BRANCH" "$REPO_URL" "$APP_DIR"
fi

# ── 6. .env check ────────────────────────────────────────────
echo ""
echo "==> [6/6] Checking for .env file at $INFRA_DIR/.env ..."
if [ ! -f "$INFRA_DIR/.env" ]; then
  echo ""
  echo "  ╔═══════════════════════════════════════════════════════╗"
  echo "  ║  ACTION REQUIRED — create your production .env file  ║"
  echo "  ╠═══════════════════════════════════════════════════════╣"
  echo "  ║  Run these two commands, then re-run this script:    ║"
  echo "  ║                                                       ║"
  echo "  ║  cp $INFRA_DIR/.env.example \\"
  echo "  ║     $INFRA_DIR/.env"
  echo "  ║                                                       ║"
  echo "  ║  nano $INFRA_DIR/.env"
  echo "  ║                                                       ║"
  echo "  ║  Change these values (everything else can stay):     ║"
  echo "  ║    POSTGRES_PASSWORD  → strong random password       ║"
  echo "  ║    JWT_SECRET         → 32+ char random string       ║"
  echo "  ║    MEILI_MASTER_KEY   → strong random string         ║"
  echo "  ║    CORS_ORIGINS       → your Vercel frontend URL     ║"
  echo "  ╚═══════════════════════════════════════════════════════╝"
  echo ""
  exit 1
fi

# ── Start services ───────────────────────────────────────────
echo ""
echo "==> Starting Docker services (this builds images, takes a few minutes)..."
cd "$INFRA_DIR"
sudo docker-compose -f docker-compose.prod.yml up -d --build

echo ""
echo "  ╔══════════════════════════════════════════════════════════╗"
echo "  ║  Services started! One manual step left: Caddy setup    ║"
echo "  ╠══════════════════════════════════════════════════════════╣"
echo "  ║                                                          ║"
echo "  ║  1. Open the Caddyfile:                                  ║"
echo "  ║     sudo nano /etc/caddy/Caddyfile                       ║"
echo "  ║                                                          ║"
echo "  ║  2. Replace the contents with:                           ║"
echo "  ║     your-subdomain.duckdns.org {                         ║"
echo "  ║         reverse_proxy localhost:8080                     ║"
echo "  ║     }                                                    ║"
echo "  ║                                                          ║"
echo "  ║  3. Reload Caddy (gets the Let's Encrypt cert):          ║"
echo "  ║     sudo systemctl reload caddy                          ║"
echo "  ║                                                          ║"
echo "  ║  Backend will then be live at:                           ║"
echo "  ║     https://your-subdomain.duckdns.org/api               ║"
echo "  ╚══════════════════════════════════════════════════════════╝"
echo ""
