export interface ApiConfig {
  baseUrl: string;
  defaultHeaders?: HeadersInit;
}

function resolveApiBaseUrl(): string {
  const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim();

  return configuredBaseUrl ? configuredBaseUrl : "http://localhost:8080";
}

export const apiConfig: ApiConfig = {
  baseUrl: resolveApiBaseUrl(),
  defaultHeaders: {
    Accept: "application/json",
  },
};
