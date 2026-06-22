export type FlightSegment = {
    flightNumber: string;
    airline: string;
    origin: string;
    destination: string;
    departureTime: string;
    arrivalTime: string;
    price: number;
    aircraft: string;
    durationMinutes: number;
};

export type Layover = {
    airport: string;
    durationMinutes: number;
    connectionType: "domestic" | "international" | string;
};

export type Itinerary = {
    segments: FlightSegment[];
    layovers: Layover[];
    totalDurationMinutes: number;
    totalPrice: number;
};

export type SearchResponse = {
    origin: string;
    destination: string;
    date: string;
    count: number;
    itineraries: Itinerary[];
};

export type ApiError = {
    timestamp?: string;
    status?: number;
    error?: string;
    message?: string;
};