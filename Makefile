.PHONY: dev build dev-frontend build-frontend infra-up infra-down infra-logs help

help:
	@echo "Ordera Platform - Make Commands"
	@echo "  make dev          - Khởi động Frontend Dev Server"
	@echo "  make build        - Build Frontend cho Production"
	@echo "  make infra-up     - Khởi động Docker Compose (PostgreSQL, Redis, MailHog, Nginx)"
	@echo "  make infra-down   - Tắt Docker Compose"
	@echo "  make infra-logs   - Xem logs của Docker containers"

dev:
	npm run dev

build:
	npm run build

infra-up:
	docker compose -f infra/docker/docker-compose.yml up -d

infra-down:
	docker compose -f infra/docker/docker-compose.yml down

infra-logs:
	docker compose -f infra/docker/docker-compose.yml logs -f

