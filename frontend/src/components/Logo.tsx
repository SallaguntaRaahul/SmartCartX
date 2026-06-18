export default function Logo({ size = 28 }: { size?: number }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
      <svg width={size} height={size} viewBox="0 0 32 32" fill="none">
        <rect width="32" height="32" rx="9" fill="var(--color-primary)" />
        <path
          d="M9 12h14l-1.4 9.2a2 2 0 0 1-2 1.8H12.4a2 2 0 0 1-2-1.8L9 12Z"
          stroke="white"
          strokeWidth="1.8"
          strokeLinejoin="round"
        />
        <path
          d="M12 12V9.5a4 4 0 0 1 8 0V12"
          stroke="white"
          strokeWidth="1.8"
          strokeLinecap="round"
        />
        <circle cx="13.5" cy="16.5" r="1.4" fill="var(--color-accent)" />
        <circle cx="18.5" cy="16.5" r="1.4" fill="var(--color-accent)" />
      </svg>
      <span
        style={{
          fontFamily: 'var(--font-display)',
          fontWeight: 800,
          fontSize: size * 0.7,
          letterSpacing: '-0.02em',
          color: 'var(--color-text)',
        }}
      >
        SmartCart<span style={{ color: 'var(--color-accent)' }}>X</span>
      </span>
    </div>
  );
}
