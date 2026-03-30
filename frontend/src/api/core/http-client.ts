import type { ApiConfig } from "../config";
import { ApiError } from "./errors";
import { buildQueryString } from "./query-string";
import type { HttpClient, RequestOptions } from "./types";

function joinUrl(baseUrl: string, path: string): string {
  const normalizedBaseUrl = baseUrl.replace(/\/+$/, "");
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;

  return `${normalizedBaseUrl}${normalizedPath}`;
}

function isBodyInit(value: unknown): value is BodyInit {
  return (
    typeof value === "string" ||
    value instanceof Blob ||
    value instanceof FormData ||
    value instanceof URLSearchParams ||
    value instanceof ArrayBuffer ||
    ArrayBuffer.isView(value)
  );
}

function createBody(
  headers: Headers,
  body: unknown,
): BodyInit | null | undefined {
  if (body === undefined) {
    return undefined;
  }

  if (isBodyInit(body)) {
    return body;
  }

  const contentType = headers.get("Content-Type") ?? "";

  if (contentType.includes("application/json")) {
    return JSON.stringify(body);
  }

  return String(body);
}

async function parseResponse<TResponse>(
  response: Response,
  responseType?: RequestOptions["responseType"],
): Promise<TResponse> {
  if (response.status === 204) {
    return undefined as TResponse;
  }

  if (responseType === "blob") {
    return response.blob() as Promise<TResponse>;
  }

  if (responseType === "text") {
    return response.text() as Promise<TResponse>;
  }

  const contentType = response.headers.get("content-type") ?? "";

  if (contentType.includes("application/json")) {
    return response.json() as Promise<TResponse>;
  }

  const text = await response.text();

  return text as TResponse;
}

async function parseErrorBody(response: Response): Promise<unknown> {
  try {
    return await parseResponse<unknown>(response);
  } catch {
    return null;
  }
}

export function createHttpClient(config: ApiConfig): HttpClient {
  async function request<TResponse, TQuery extends object, TBody = unknown>(
    method: string,
    path: string,
    options: RequestOptions<TQuery, TBody> = {},
  ): Promise<TResponse> {
    const url = `${joinUrl(config.baseUrl, path)}${buildQueryString(options.query)}`;
    const headers = new Headers(config.defaultHeaders);

    if (options.headers) {
      new Headers(options.headers).forEach((value, key) => {
        headers.set(key, value);
      });
    }

    if (options.body !== undefined && !isBodyInit(options.body)) {
      const contentType = headers.get("Content-Type");

      if (!contentType) {
        headers.set("Content-Type", "application/json");
      }
    }

    const body = createBody(headers, options.body);

    const response = await fetch(url, {
      method,
      body,
      headers,
      signal: options.signal,
    });

    if (!response.ok) {
      const body = await parseErrorBody(response);

      throw new ApiError({
        body,
        method,
        path,
        status: response.status,
        statusText: response.statusText,
      });
    }

    return parseResponse<TResponse>(response, options.responseType);
  }

  return {
    get<TResponse, TQuery extends object>(
      path: string,
      options?: RequestOptions<TQuery>,
    ) {
      return request<TResponse, TQuery>("GET", path, options);
    },
    post<TResponse, TBody, TQuery extends object>(
      path: string,
      options?: RequestOptions<TQuery, TBody>,
    ) {
      return request<TResponse, TQuery, TBody>("POST", path, options);
    },
  };
}
