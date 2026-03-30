import type { QueryParamPrimitive } from "./types";

function appendValue(
  searchParams: URLSearchParams,
  key: string,
  value: string | number | boolean,
): void {
  searchParams.append(key, String(value));
}

function isQueryParamPrimitive(
  value: unknown,
): value is Exclude<QueryParamPrimitive, null | undefined> {
  return (
    typeof value === "boolean" ||
    typeof value === "number" ||
    typeof value === "string"
  );
}

export function buildQueryString(query?: object): string {
  if (!query) {
    return "";
  }

  const searchParams = new URLSearchParams();

  for (const [key, rawValue] of Object.entries(query)) {
    if (rawValue == null) {
      continue;
    }

    if (Array.isArray(rawValue)) {
      for (const item of rawValue) {
        if (isQueryParamPrimitive(item)) {
          appendValue(searchParams, key, item);
        }
      }

      continue;
    }

    if (isQueryParamPrimitive(rawValue)) {
      appendValue(searchParams, key, rawValue);
    }
  }

  const queryString = searchParams.toString();

  return queryString ? `?${queryString}` : "";
}
