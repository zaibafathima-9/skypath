export type SearchFormValues = {
    origin: string;
    destination: string;
    date: string;
};

export type SearchFormErrors = Partial<Record<keyof SearchFormValues, string>>;