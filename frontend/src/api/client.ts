import { apiConfig } from "./config";
import { createHttpClient } from "./core/http-client";
import { createApi } from "./create-api";

const httpClient = createHttpClient(apiConfig);

export const api = createApi(httpClient);
