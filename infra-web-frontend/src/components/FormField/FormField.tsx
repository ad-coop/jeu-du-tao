import styles from "./FormField.module.css";

type FormFieldProps = {
  id: string;
  label: string;
  type?: string;
  value: string;
  onChange: (value: string) => void;
  error?: string;
  hint?: string;
  required?: boolean;
  disabled?: boolean;
  placeholder?: string;
};

export function FormField({
  id,
  label,
  type = "text",
  value,
  onChange,
  error,
  hint,
  required = false,
  disabled = false,
  placeholder,
}: FormFieldProps) {
  const errorId = `${id}-error`;
  const hintId = `${id}-hint`;

  const describedBy = [error ? errorId : null, hint ? hintId : null]
    .filter(Boolean)
    .join(" ");

  return (
    <div className={styles.field}>
      <label htmlFor={id} className={styles.label}>
        {label}
        {required && (
          <span className={styles.required} aria-hidden="true">
            {" "}*
          </span>
        )}
      </label>
      <input
        id={id}
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className={`${styles.input} ${error ? styles.inputError : ""}`}
        aria-describedby={describedBy || undefined}
        aria-invalid={error ? true : undefined}
        required={required}
        disabled={disabled}
        placeholder={placeholder}
      />
      {error && (
        <span id={errorId} role="alert" className={styles.error}>
          {error}
        </span>
      )}
      {hint && (
        <span id={hintId} className={styles.hint}>
          {hint}
        </span>
      )}
    </div>
  );
}
