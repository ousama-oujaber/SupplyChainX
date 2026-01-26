#!/bin/bash

# SupplyChainX DigitalOcean Deployment Script
# This script helps you deploy to your DigitalOcean droplet

set -e

echo "🚀 SupplyChainX DigitalOcean Deployment Helper"
echo "=============================================="
echo ""

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo "⚠️  No .env file found!"
    echo "Creating .env from template..."
    cp .env.production.example .env
    echo "✅ Created .env file"
    echo ""
    echo "📝 IMPORTANT: Edit the .env file with your actual values before continuing!"
    echo "   nano .env"
    echo ""
    read -p "Press Enter after you've edited the .env file..."
fi

# Ask for deployment action
echo ""
echo "What would you like to do?"
echo "1) Deploy/Update application"
echo "2) View logs"
echo "3) Stop application"
echo "4) Restart application"
echo "5) Backup database"
echo "6) Clean up old images"
echo ""
read -p "Enter your choice (1-6): " choice

case $choice in
    1)
        echo "🔄 Pulling latest images..."
        docker compose -f docker-compose.prod.yml pull
        
        echo "🚀 Starting services..."
        docker compose -f docker-compose.prod.yml up -d
        
        echo "✅ Deployment complete!"
        echo ""
        echo "📊 Service status:"
        docker compose -f docker-compose.prod.yml ps
        ;;
    
    2)
        echo "📋 Viewing logs (Ctrl+C to exit)..."
        docker compose -f docker-compose.prod.yml logs -f
        ;;
    
    3)
        echo "🛑 Stopping services..."
        docker compose -f docker-compose.prod.yml down
        echo "✅ Services stopped"
        ;;
    
    4)
        echo "🔄 Restarting services..."
        docker compose -f docker-compose.prod.yml restart
        echo "✅ Services restarted"
        ;;
    
    5)
        BACKUP_FILE="backup_$(date +%Y%m%d_%H%M%S).sql"
        echo "💾 Creating database backup: $BACKUP_FILE"
        
        # Get MySQL password from .env
        source .env
        
        docker exec supplychainx-mysql mysqldump -u ${MYSQL_USER} -p${MYSQL_PASSWORD} ${MYSQL_DATABASE} > $BACKUP_FILE
        
        echo "✅ Backup created: $BACKUP_FILE"
        ;;
    
    6)
        echo "🧹 Cleaning up old images..."
        docker image prune -a -f
        echo "✅ Cleanup complete"
        
        echo ""
        echo "💾 Disk usage:"
        docker system df
        ;;
    
    *)
        echo "❌ Invalid choice"
        exit 1
        ;;
esac

echo ""
echo "🎉 Done!"
