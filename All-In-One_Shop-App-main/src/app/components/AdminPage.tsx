import { useState } from 'react';
import { useNavigate } from 'react-router';
import { ShieldAlert, Package, Store, Tag, DollarSign, TrendingUp, Play, Square, RefreshCw, ArrowLeft } from 'lucide-react';
import { Button } from './ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Skeleton } from './ui/skeleton';
import { Badge } from './ui/badge';
import { useAuth } from '../context/AuthContext';
import { useAdminStats, useFakerStatus, useRunScraper, useStartFaker, useStopFaker } from '../api/hooks';
import { toast } from 'sonner';

function StatCard({ icon: Icon, label, value, color }: {
  icon: React.ComponentType<{ className?: string }>;
  label: string;
  value: string | number | undefined;
  color: string;
}) {
  return (
    <Card>
      <CardContent className="flex items-center gap-4 pt-6">
        <div className={`p-3 rounded-full ${color}`}>
          <Icon className="h-6 w-6 text-white" />
        </div>
        <div>
          <p className="text-sm text-muted-foreground">{label}</p>
          {value !== undefined ? (
            <p className="text-2xl font-bold">{value}</p>
          ) : (
            <Skeleton className="h-8 w-20 mt-1" />
          )}
        </div>
      </CardContent>
    </Card>
  );
}

export function AdminPage() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const { data: stats, isLoading: statsLoading, mutate: refreshStats } = useAdminStats();
  const { data: fakerStatus, mutate: refreshFakerStatus } = useFakerStatus();
  const { trigger: runScraper, isMutating: scraperRunning } = useRunScraper();
  const { trigger: startFaker, isMutating: fakerStarting } = useStartFaker();
  const { trigger: stopFaker, isMutating: fakerStopping } = useStopFaker();

  const [intervalMs, setIntervalMs] = useState(3000);
  const [batchSize, setBatchSize] = useState(5);

  if (user?.role !== 'ADMIN') {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center space-y-4">
          <ShieldAlert className="h-16 w-16 text-red-500 mx-auto" />
          <h1 className="text-2xl font-bold">Access Denied</h1>
          <p className="text-muted-foreground">You need admin privileges to view this page.</p>
          <Button onClick={() => navigate('/')}>Go Home</Button>
        </div>
      </div>
    );
  }

  const handleRunScraper = async () => {
    try {
      await runScraper();
      await refreshStats();
      toast.success('Scraper triggered successfully');
    } catch {
      toast.error('Failed to trigger scraper');
    }
  };

  const handleStartFaker = async () => {
    try {
      await startFaker({ intervalMs, batchSize });
      await refreshFakerStatus();
      toast.success(`Faker started — generating ${batchSize} products every ${intervalMs}ms`);
    } catch {
      toast.error('Failed to start faker');
    }
  };

  const handleStopFaker = async () => {
    try {
      await stopFaker();
      await refreshFakerStatus();
      toast.success('Faker stopped');
    } catch {
      toast.error('Failed to stop faker');
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white border-b sticky top-0 z-10">
        <div className="max-w-6xl mx-auto px-4 py-4 flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={() => navigate('/')}>
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <div className="flex items-center gap-2">
            <ShieldAlert className="h-6 w-6 text-blue-600" />
            <h1 className="text-xl font-bold">Admin Dashboard</h1>
          </div>
          <Badge variant="secondary" className="ml-auto">
            {user.email}
          </Badge>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 py-8 space-y-8">

        {/* Stats */}
        <section>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold">Overview</h2>
            <Button variant="outline" size="sm" onClick={() => refreshStats()} disabled={statsLoading}>
              <RefreshCw className={`h-4 w-4 mr-2 ${statsLoading ? 'animate-spin' : ''}`} />
              Refresh
            </Button>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            <StatCard icon={Package} label="Total Products" value={stats?.totalProducts} color="bg-blue-500" />
            <StatCard icon={Store} label="Active Stores" value={stats?.activeStores} color="bg-green-500" />
            <StatCard icon={Tag} label="Total Brands" value={stats?.totalBrands} color="bg-purple-500" />
            <StatCard icon={DollarSign} label="Total Prices" value={stats?.totalPrices} color="bg-yellow-500" />
            <StatCard
              icon={TrendingUp}
              label="Avg Price"
              value={stats?.avgPrice !== undefined ? `€${stats.avgPrice.toFixed(2)}` : undefined}
              color="bg-orange-500"
            />
            <Card>
              <CardContent className="pt-6">
                <p className="text-sm text-muted-foreground">Top Priced Product</p>
                {stats ? (
                  <p className="text-sm font-medium mt-1 truncate" title={stats.topPriceProduct}>
                    {stats.topPriceProduct}
                  </p>
                ) : (
                  <Skeleton className="h-5 w-full mt-1" />
                )}
              </CardContent>
            </Card>
          </div>
        </section>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

          {/* Scraper */}
          <Card>
            <CardHeader>
              <CardTitle>Scraper</CardTitle>
              <CardDescription>
                Trigger the product scraper to pull fresh data from all connected stores.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Button onClick={handleRunScraper} disabled={scraperRunning} className="w-full">
                {scraperRunning ? (
                  <RefreshCw className="h-4 w-4 mr-2 animate-spin" />
                ) : (
                  <RefreshCw className="h-4 w-4 mr-2" />
                )}
                {scraperRunning ? 'Triggering…' : 'Run Scraper'}
              </Button>
            </CardContent>
          </Card>

          {/* Faker */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                Fake Data Generator
                {fakerStatus?.generating && (
                  <Badge className="bg-green-500 text-white animate-pulse">Running</Badge>
                )}
              </CardTitle>
              <CardDescription>
                Generate fake products at a configurable rate for testing and demos.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1">
                  <Label htmlFor="intervalMs">Interval (ms)</Label>
                  <Input
                    id="intervalMs"
                    type="number"
                    min={500}
                    step={500}
                    value={intervalMs}
                    onChange={(e) => setIntervalMs(Number(e.target.value))}
                    disabled={fakerStatus?.generating}
                  />
                </div>
                <div className="space-y-1">
                  <Label htmlFor="batchSize">Batch Size</Label>
                  <Input
                    id="batchSize"
                    type="number"
                    min={1}
                    max={50}
                    value={batchSize}
                    onChange={(e) => setBatchSize(Number(e.target.value))}
                    disabled={fakerStatus?.generating}
                  />
                </div>
              </div>
              <div className="flex gap-2">
                <Button
                  onClick={handleStartFaker}
                  disabled={fakerStatus?.generating || fakerStarting}
                  className="flex-1"
                >
                  <Play className="h-4 w-4 mr-2" />
                  {fakerStarting ? 'Starting…' : 'Start'}
                </Button>
                <Button
                  variant="destructive"
                  onClick={handleStopFaker}
                  disabled={!fakerStatus?.generating || fakerStopping}
                  className="flex-1"
                >
                  <Square className="h-4 w-4 mr-2" />
                  {fakerStopping ? 'Stopping…' : 'Stop'}
                </Button>
              </div>
            </CardContent>
          </Card>

        </div>
      </main>
    </div>
  );
}
