import { useState, useCallback } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from './ui/dialog';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { useAuth } from '../context/AuthContext';
import { toast } from 'sonner';

interface AuthModalProps {
  isOpen: boolean;
  onClose: () => void;
}

// ── Validation helpers ──────────────────────────────────────
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

interface FieldErrors {
  email?: string;
  password?: string;
  firstName?: string;
  lastName?: string;
}

function validateEmail(email: string): string | undefined {
  if (!email.trim()) return 'Email is required';
  if (!EMAIL_REGEX.test(email)) return 'Please enter a valid email address';
  return undefined;
}

function validatePassword(password: string, isRegister: boolean): string | undefined {
  if (!password) return 'Password is required';
  if (isRegister && password.length < 8) return 'Password must be at least 8 characters';
  return undefined;
}

function validateFirstName(firstName: string, isRegister: boolean): string | undefined {
  if (!isRegister) return undefined;
  if (!firstName.trim()) return 'First name is required';
  if (firstName.trim().length > 100) return 'First name cannot exceed 100 characters';
  return undefined;
}

function validateLastName(lastName: string, isRegister: boolean): string | undefined {
  if (!isRegister) return undefined;
  if (!lastName.trim()) return 'Last name is required';
  if (lastName.trim().length > 100) return 'Last name cannot exceed 100 characters';
  return undefined;
}

// ── Component ───────────────────────────────────────────────
export function AuthModal({ isOpen, onClose }: AuthModalProps) {
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [isLoading, setIsLoading] = useState(false);
  const { login, register } = useAuth();

  const [formData, setFormData] = useState({
    email: '',
    password: '',
    firstName: '',
    lastName: '',
  });

  const [errors, setErrors] = useState<FieldErrors>({});
  const [touched, setTouched] = useState<Record<string, boolean>>({});

  const isRegister = mode === 'register';

  // Run full validation and return true if form is valid
  const validateAll = useCallback((): boolean => {
    const newErrors: FieldErrors = {
      email: validateEmail(formData.email),
      password: validatePassword(formData.password, isRegister),
      firstName: validateFirstName(formData.firstName, isRegister),
      lastName: validateLastName(formData.lastName, isRegister),
    };
    setErrors(newErrors);
    // Mark all fields as touched so errors show
    setTouched({ email: true, password: true, firstName: true, lastName: true });
    return !Object.values(newErrors).some(Boolean);
  }, [formData, isRegister]);

  // Validate a single field (used on blur)
  const validateField = useCallback(
    (name: string, value: string) => {
      let error: string | undefined;
      switch (name) {
        case 'email':
          error = validateEmail(value);
          break;
        case 'password':
          error = validatePassword(value, isRegister);
          break;
        case 'firstName':
          error = validateFirstName(value, isRegister);
          break;
        case 'lastName':
          error = validateLastName(value, isRegister);
          break;
      }
      setErrors(prev => ({ ...prev, [name]: error }));
    },
    [isRegister],
  );

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Client-side gate — prevents submission if invalid
    if (!validateAll()) return;

    setIsLoading(true);

    try {
      if (mode === 'login') {
        await login({ email: formData.email, password: formData.password });
        toast.success('Welcome back!');
      } else {
        await register(formData);
        toast.success('Account created successfully!');
      }
      onClose();
      setFormData({ email: '', password: '', firstName: '', lastName: '' });
      setErrors({});
      setTouched({});
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'Authentication failed');
    } finally {
      setIsLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    // Clear error as user types (re-validate on blur)
    if (touched[name]) {
      validateField(name, value);
    }
  };

  const handleBlur = (e: React.FocusEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setTouched(prev => ({ ...prev, [name]: true }));
    validateField(name, value);
  };

  const errorStyle = 'text-xs text-red-500 mt-1';
  const inputErrorClass = (field: keyof FieldErrors) =>
    touched[field] && errors[field] ? 'border-red-500 focus-visible:ring-red-500' : '';

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle className="text-2xl font-bold text-center">
            {mode === 'login' ? 'Welcome Back' : 'Create Account'}
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4 mt-4" noValidate>
          {isRegister && (
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="firstName">First Name</Label>
                <Input
                  id="firstName"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleInputChange}
                  onBlur={handleBlur}
                  placeholder="John"
                  className={inputErrorClass('firstName')}
                />
                {touched.firstName && errors.firstName && (
                  <p className={errorStyle}>{errors.firstName}</p>
                )}
              </div>
              <div className="space-y-2">
                <Label htmlFor="lastName">Last Name</Label>
                <Input
                  id="lastName"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleInputChange}
                  onBlur={handleBlur}
                  placeholder="Doe"
                  className={inputErrorClass('lastName')}
                />
                {touched.lastName && errors.lastName && (
                  <p className={errorStyle}>{errors.lastName}</p>
                )}
              </div>
            </div>
          )}

          <div className="space-y-2">
            <Label htmlFor="email">Email</Label>
            <Input
              id="email"
              name="email"
              type="email"
              value={formData.email}
              onChange={handleInputChange}
              onBlur={handleBlur}
              placeholder="john@example.com"
              className={inputErrorClass('email')}
            />
            {touched.email && errors.email && (
              <p className={errorStyle}>{errors.email}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="password">Password</Label>
            <Input
              id="password"
              name="password"
              type="password"
              value={formData.password}
              onChange={handleInputChange}
              onBlur={handleBlur}
              placeholder={isRegister ? 'Min. 8 characters' : 'Enter your password'}
              className={inputErrorClass('password')}
            />
            {touched.password && errors.password && (
              <p className={errorStyle}>{errors.password}</p>
            )}
          </div>

          <Button type="submit" className="w-full" disabled={isLoading}>
            {isLoading
              ? 'Please wait...'
              : mode === 'login'
              ? 'Sign In'
              : 'Create Account'}
          </Button>
        </form>

        <div className="text-center text-sm text-slate-600 mt-4">
          {mode === 'login' ? (
            <>
              Don't have an account?{' '}
              <button
                type="button"
                onClick={() => {
                  setMode('register');
                  setErrors({});
                  setTouched({});
                }}
                className="text-blue-600 hover:underline font-medium"
              >
                Sign up
              </button>
            </>
          ) : (
            <>
              Already have an account?{' '}
              <button
                type="button"
                onClick={() => {
                  setMode('login');
                  setErrors({});
                  setTouched({});
                }}
                className="text-blue-600 hover:underline font-medium"
              >
                Sign in
              </button>
            </>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}
