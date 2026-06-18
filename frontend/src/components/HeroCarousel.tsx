import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

interface Slide {
  title: string;
  subtitle: string;
  ctaText: string;
  ctaLink: string;
  gradient: string;
}

const SLIDES: Slide[] = [
  {
    title: 'Shopping that gets smarter with every order.',
    subtitle:
      'SmartCartX learns what you like and surfaces it instantly — powered by real-time AI recommendations.',
    ctaText: 'Browse products',
    ctaLink: '/products',
    gradient: 'linear-gradient(135deg, #4F3FF0 0%, #6450FF 60%, #FF8A3D 130%)',
  },
  {
    title: 'Over 2,000 products. One smart cart.',
    subtitle:
      'From electronics to home essentials — everything you need, organized and easy to find.',
    ctaText: 'Explore catalog',
    ctaLink: '/products',
    gradient: 'linear-gradient(135deg, #1E8E5A 0%, #1AAE74 60%, #4F3FF0 130%)',
  },
  {
    title: 'AI that watches your cart, not your wallet.',
    subtitle:
      'Get instant order summaries and anomaly checks powered by real LLM reasoning on every purchase.',
    ctaText: 'See it in action',
    ctaLink: '/register',
    gradient: 'linear-gradient(135deg, #D7373F 0%, #FF8A3D 60%, #4F3FF0 130%)',
  },
];

export default function HeroCarousel() {
  const [index, setIndex] = useState(0);

  useEffect(() => {
    const timer = setInterval(() => {
      setIndex((i) => (i + 1) % SLIDES.length);
    }, 5000);
    return () => clearInterval(timer);
  }, []);

  const slide = SLIDES[index];

  return (
    <section
      style={{
        background: slide.gradient,
        color: 'white',
        padding: '90px 24px 100px',
        position: 'relative',
        overflow: 'hidden',
        transition: 'background 0.6s ease',
      }}
    >
      <div
        key={index}
        className="container animate-fade-up"
        style={{ maxWidth: 720, textAlign: 'center' }}
      >
        <h1 style={{ fontSize: 44, lineHeight: 1.15, marginBottom: 18 }}>
          {slide.title}
        </h1>
        <p style={{ fontSize: 17, opacity: 0.92, marginBottom: 32, lineHeight: 1.6 }}>
          {slide.subtitle}
        </p>
        <div style={{ display: 'flex', gap: 12, justifyContent: 'center' }}>
          <Link
            to={slide.ctaLink}
            className="btn hover-lift"
            style={{
              background: 'white',
              color: 'var(--color-primary)',
              padding: '14px 28px',
              fontSize: 15,
            }}
          >
            {slide.ctaText}
          </Link>
          <Link
            to="/register"
            className="btn hover-lift"
            style={{
              background: 'rgba(255,255,255,0.15)',
              color: 'white',
              border: '1.5px solid rgba(255,255,255,0.4)',
              padding: '14px 28px',
              fontSize: 15,
            }}
          >
            Create free account
          </Link>
        </div>
      </div>

      <div
        style={{
          position: 'absolute',
          bottom: 24,
          left: 0,
          right: 0,
          display: 'flex',
          justifyContent: 'center',
          gap: 8,
        }}
      >
        {SLIDES.map((_, i) => (
          <button
            key={i}
            onClick={() => setIndex(i)}
            aria-label={`Go to slide ${i + 1}`}
            style={{
              width: i === index ? 24 : 8,
              height: 8,
              borderRadius: 4,
              border: 'none',
              background: i === index ? 'white' : 'rgba(255,255,255,0.4)',
              transition: 'all 0.3s ease',
              cursor: 'pointer',
            }}
          />
        ))}
      </div>
    </section>
  );
}
