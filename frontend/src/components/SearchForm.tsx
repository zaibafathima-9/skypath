import type { FormEvent } from "react";
import type { SearchFormErrors, SearchFormValues } from "../types/search";

type SearchFormProps = {
    values: SearchFormValues;
    errors: SearchFormErrors;
    isLoading: boolean;
    onChange: (field: keyof SearchFormValues, value: string) => void;
    onSubmit: () => void;
};

export function SearchForm({
                               values,
                               errors,
                               isLoading,
                               onChange,
                               onSubmit,
                           }: SearchFormProps) {
    function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        onSubmit();
    }

    return (
        <form className="search-form" onSubmit={handleSubmit}>
            <div className="field">
                <label htmlFor="origin">Origin</label>
                <input
                    id="origin"
                    value={values.origin}
                    maxLength={3}
                    placeholder="JFK"
                    onChange={(event) => onChange("origin", event.target.value.toUpperCase())}
                />
                {errors.origin && <p className="field-error">{errors.origin}</p>}
            </div>

            <div className="field">
                <label htmlFor="destination">Destination</label>
                <input
                    id="destination"
                    value={values.destination}
                    maxLength={3}
                    placeholder="LAX"
                    onChange={(event) =>
                        onChange("destination", event.target.value.toUpperCase())
                    }
                />
                {errors.destination && (
                    <p className="field-error">{errors.destination}</p>
                )}
            </div>

            <div className="field">
                <label htmlFor="date">Date</label>
                <input
                    id="date"
                    type="date"
                    value={values.date}
                    onChange={(event) => onChange("date", event.target.value)}
                />
                {errors.date && <p className="field-error">{errors.date}</p>}
            </div>

            <button type="submit" disabled={isLoading}>
                {isLoading ? "Searching..." : "Search flights"}
            </button>
        </form>
    );
}