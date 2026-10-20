import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { setToken } from './api';

export default function OAuthCallback() {
  const navigate = useNavigate();
  const [error, setError] = useState('');

  useEffect(() => {
    const hash = window.location.hash;
    const params = new URLSearchParams(hash.replace(/^#/, ''));
    const jwt = params.get('token');

    // Remove the token from the browser URL immediately.
    window.history.replaceState(
      null,
      '',
      window.location.pathname
    );

    if (!jwt) {
      setError('Google sign-in failed: no token was received.');
      return;
    }

    setToken(jwt);
    window.dispatchEvent(new Event('auth-change'));
    navigate('/', { replace: true });
  }, [navigate]);

  if (error) {
    return (
      <main style={{ padding: '3rem', textAlign: 'center' }}>
        <h2>Sign-in failed</h2>
        <p>{error}</p>
        <a href="/login">Return to sign in</a>
      </main>
    );
  }

  return (
    <main style={{ padding: '3rem', textAlign: 'center' }}>
      <p>Completing Google sign-in…</p>
    </main>
  );
}
