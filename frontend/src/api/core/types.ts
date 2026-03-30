export type QueryParamPrimitive = string | number | boolean | null | undefined;
export type QueryParamValue = QueryParamPrimitive | QueryParamPrimitive[];
export type QueryParams = Record<string, QueryParamValue>;
export type ResponseType = "blob" | "json" | "text";

export interface RequestConfig {
  headers?: HeadersInit;
  signal?: AbortSignal;
}

export interface RequestOptions<
  TQuery extends object = QueryParams,
  TBody = unknown,
> extends RequestConfig {
  body?: TBody;
  responseType?: ResponseType;
  query?: TQuery;
}

export interface HttpClient {
  get<TResponse, TQuery extends object = QueryParams>(
    path: string,
    options?: RequestOptions<TQuery>,
  ): Promise<TResponse>;
  post<
    TResponse,
    TBody = unknown,
    TQuery extends object = QueryParams,
  >(
    path: string,
    options?: RequestOptions<TQuery, TBody>,
  ): Promise<TResponse>;
}
