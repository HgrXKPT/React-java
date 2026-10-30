function getEnv(key: string, fallback = ""): string {
  return import.meta.env[key] ?? fallback;
}

export const env = {
  apiBaseUrl: getEnv("VITE_API_BASE_URL", "http://localhost:8080"),
};
