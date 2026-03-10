import { act, fireEvent, render, screen } from "@testing-library/react";
import { ToastProvider } from "./ToastProvider";
import { useToast } from "./useToast";

function ToastTrigger({ message }: { message: string }) {
  const { showToast } = useToast();
  return <button onClick={() => showToast(message)}>Show Toast</button>;
}

describe("Toast", () => {
  it("showToast_displaysMessage", () => {
    render(
      <ToastProvider>
        <ToastTrigger message="Hello" />
      </ToastProvider>,
    );

    fireEvent.click(screen.getByText("Show Toast"));

    expect(screen.getByText("Hello")).toBeInTheDocument();
  });

  it("toast_hasAriaLiveAttribute", () => {
    render(
      <ToastProvider>
        <div />
      </ToastProvider>,
    );

    expect(screen.getByRole("status")).toHaveAttribute("aria-live", "polite");
  });

  it("toast_autoDismisses", () => {
    vi.useFakeTimers();

    render(
      <ToastProvider>
        <ToastTrigger message="Auto dismiss" />
      </ToastProvider>,
    );

    fireEvent.click(screen.getByText("Show Toast"));
    expect(screen.getByText("Auto dismiss")).toBeInTheDocument();

    act(() => {
      vi.advanceTimersByTime(3100);
    });

    expect(screen.queryByText("Auto dismiss")).not.toBeInTheDocument();

    vi.useRealTimers();
  });
});
