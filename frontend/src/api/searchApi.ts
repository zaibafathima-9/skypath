import type { ApiError, SearchResponse } from "../types/flight";
import type { SearchFormValues } from "../types/search";

const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export async function searchFlights(
    values: SearchFormValues
): Promise<SearchResponse> {
    const params = new URLSearchParams({
        origin: values.origin.trim().toUpperCase(),
        destination: values.destination.trim().toUpperCase(),
        date: values.date,
    });

    const response = await fetch(`${API_BASE_URL}/api/search?${params.toString()}`);

    if (!response.ok) {
        let apiError: ApiError = {};

        try {
            apiError = await response.json();
        } catch {
            apiError = {
                message: "Unable to search flights. Please try again.",
            };
        }

        throw new Error(
            apiError.message || "Unable to search flights. Please try again."
        );
    }

    return response.json();
}