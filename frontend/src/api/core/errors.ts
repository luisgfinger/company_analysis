export interface ApiErrorDetails {
  body: unknown;
  method: string;
  path: string;
  status: number;
  statusText: string;
}

export class ApiError extends Error {
  readonly body: unknown;
  readonly method: string;
  readonly path: string;
  readonly status: number;
  readonly statusText: string;

  constructor(details: ApiErrorDetails) {
    super(`HTTP ${details.status} ${details.statusText}`);

    this.name = "ApiError";
    this.body = details.body;
    this.method = details.method;
    this.path = details.path;
    this.status = details.status;
    this.statusText = details.statusText;
  }
}
