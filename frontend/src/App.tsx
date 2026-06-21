import { useState } from "react";
import { SearchForm } from "./components/SearchForm";
import type { SearchFormErrors, SearchFormValues } from "./types/search";

const DEFAULT_FORM: SearchFormValues = {
  origin: "JFK",
  destination: "LAX",
  date: "2024-03-15",
};

function validate(values: SearchFormValues): SearchFormErrors {
  const errors: SearchFormErrors = {};

  if (!values.origin.trim()) {
    errors.origin = "Origin is required.";
  } else if (!/^[A-Z]{3}$/.test(values.origin.trim())) {
    errors.origin = "Origin must be a 3-letter IATA code.";
  }

  if (!values.destination.trim()) {
    errors.destination = "Destination is required.";
  } else if (!/^[A-Z]{3}$/.test(values.destination.trim())) {
    errors.destination = "Destination must be a 3-letter IATA code.";
  }

  if (
      values.origin.trim() &&
      values.destination.trim() &&
      values.origin.trim() === values.destination.trim()
  ) {
    errors.destination = "Destination must be different from origin.";
  }

  if (!values.date) {
    errors.date = "Date is required.";
  }

  return errors;
}

function App() {
  const [formValues, setFormValues] = useState<SearchFormValues>(DEFAULT_FORM);
  const [errors, setErrors] = useState<SearchFormErrors>({});
  const [isLoading] = useState(false);

  function handleChange(field: keyof SearchFormValues, value: string) {
    setFormValues((current) => ({
      ...current,
      [field]: value,
    }));

    setErrors((current) => ({
      ...current,
      [field]: undefined,
    }));
  }

  function handleSubmit() {
    const validationErrors = validate(formValues);
    setErrors(validationErrors);

    if (Object.keys(validationErrors).length > 0) {
      return;
    }

    console.log("Search submitted", formValues);
  }

  return (
      <main className="app">
        <section className="hero">
          <p className="eyebrow">SkyPath</p>
          <h1>Find flight connections</h1>
          <p>
            Search direct, 1-stop, and 2-stop flight itineraries with timezone-aware
            durations.
          </p>
        </section>

        <SearchForm
            values={formValues}
            errors={errors}
            isLoading={isLoading}
            onChange={handleChange}
            onSubmit={handleSubmit}
        />
      </main>
  );
}

export default App;