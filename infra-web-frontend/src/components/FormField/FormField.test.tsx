import { fireEvent, render, screen } from "@testing-library/react";
import { FormField } from "./FormField";

describe("FormField", () => {
  it("render_showsLabelAndInput", () => {
    render(
      <FormField id="name" label="Your name" value="" onChange={() => {}} />,
    );

    expect(screen.getByLabelText("Your name")).toBeInTheDocument();
    expect(screen.getByRole("textbox", { name: "Your name" })).toBeInTheDocument();
  });

  it("render_whenRequired_showsRequiredIndicator", () => {
    render(
      <FormField id="name" label="Your name" value="" onChange={() => {}} required />,
    );

    expect(screen.getByText("*", { exact: false })).toBeInTheDocument();
  });

  it("render_whenError_showsErrorWithAlertRole", () => {
    render(
      <FormField
        id="name"
        label="Your name"
        value=""
        onChange={() => {}}
        error="This field is required"
      />,
    );

    const error = screen.getByRole("alert");
    expect(error).toHaveTextContent("This field is required");
  });

  it("render_whenError_inputHasAriaDescribedBy", () => {
    render(
      <FormField
        id="name"
        label="Your name"
        value=""
        onChange={() => {}}
        error="This field is required"
      />,
    );

    const input = screen.getByRole("textbox");
    expect(input).toHaveAttribute("aria-describedby", "name-error");
    expect(input).toHaveAttribute("aria-invalid", "true");
  });

  it("render_whenHint_showsHintText", () => {
    render(
      <FormField
        id="email"
        label="Email"
        value=""
        onChange={() => {}}
        hint="We'll send game info here"
      />,
    );

    expect(screen.getByText("We'll send game info here")).toBeInTheDocument();
  });

  it("onChange_whenUserTypes_callsCallback", () => {
    const handleChange = vi.fn();
    render(
      <FormField id="name" label="Your name" value="" onChange={handleChange} />,
    );

    fireEvent.change(screen.getByRole("textbox"), { target: { value: "Alice" } });

    expect(handleChange).toHaveBeenCalledWith("Alice");
  });

  it("render_whenDisabled_inputIsDisabled", () => {
    render(
      <FormField id="name" label="Your name" value="" onChange={() => {}} disabled />,
    );

    expect(screen.getByRole("textbox")).toBeDisabled();
  });
});
