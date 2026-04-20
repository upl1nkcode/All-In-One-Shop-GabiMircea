import { Button } from './ui/button';
import { useNavigate } from 'react-router';

export function NotFound() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50">
      <div className="text-center">
        <h1 className="text-4xl font-bold mb-4 text-slate-900">404 - Page Not Found</h1>
        <p className="text-slate-600 mb-6">The page you're looking for doesn't exist.</p>
        <Button onClick={() => navigate('/')}>Go back home</Button>
      </div>
    </div>
  );
}
