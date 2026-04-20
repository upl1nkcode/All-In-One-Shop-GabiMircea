import { createBrowserRouter } from 'react-router';
import { LandingPage } from './components/LandingPage';
import { SearchResults } from './components/SearchResults';
import { ProductDetail } from './components/ProductDetail';
import { FavoritesPage } from './components/FavoritesPage';
import { NotFound } from './components/NotFound';

export const router = createBrowserRouter([
  {
    path: '/',
    Component: LandingPage,
  },
  {
    path: '/search',
    Component: SearchResults,
  },
  {
    path: '/product/:id',
    Component: ProductDetail,
  },
  {
    path: '/favorites',
    Component: FavoritesPage,
  },
  {
    path: '*',
    Component: NotFound,
  },
]);
