import React, {
  createContext,
  useContext,
  useState,
  ReactNode,
  useEffect,
} from 'react';
import { login as apiLogin, register as apiRegister } from '../api/auth';

interface AuthUser {
  email: string;
  firstName: string;
  lastName: string;
}

interface AuthContextType {
  user: AuthUser | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (data: {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
  }) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [token, setToken] = useState<string | null>(null);

  useEffect(() => {
    const storedToken = localStorage.getItem('smartcartx_token');
    const storedUser = localStorage.getItem('smartcartx_user');
    if (storedToken && storedUser) {
      setToken(storedToken);
      setUser(JSON.parse(storedUser));
    }
  }, []);

  const persistSession = (
    newToken: string,
    newUser: AuthUser
  ) => {
    localStorage.setItem('smartcartx_token', newToken);
    localStorage.setItem('smartcartx_user', JSON.stringify(newUser));
    setToken(newToken);
    setUser(newUser);
  };

  const login = async (email: string, password: string) => {
    const res = await apiLogin(email, password);
    const { token: newToken, email: e, firstName, lastName } = res.data;
    persistSession(newToken, { email: e, firstName, lastName });
  };

  const register = async (data: {
    firstName: string;
    lastName: string;
    email: string;
    password: string;
  }) => {
    // Register only — do NOT auto-login.
    // The user must sign in manually afterward, like a real site.
    await apiRegister(data);
  };

  const logout = () => {
    localStorage.removeItem('smartcartx_token');
    localStorage.removeItem('smartcartx_user');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token,
        login,
        register,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
