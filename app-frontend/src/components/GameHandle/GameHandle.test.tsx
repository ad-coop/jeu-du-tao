import { act, fireEvent, render, screen } from "@testing-library/react";
import { GameHandle } from "./GameHandle";

describe("GameHandle", () => {
  beforeEach(() => {
    vi.stubGlobal(
      "navigator",
      Object.assign({}, navigator, {
        clipboard: { writeText: vi.fn().mockResolvedValue(undefined) },
      }),
    );
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("render_showsHandle", () => {
    render(
      <GameHandle
        handle="ABC123"
        isPasswordProtected={false}
        copyLabel="Copy game code"
        copiedLabel="Copied!"
        passwordProtectedLabel="Password protected game"
      />,
    );

    expect(screen.getByText("ABC123")).toBeInTheDocument();
  });

  it("render_whenPasswordProtected_showsAccessibleLockLabel", () => {
    render(
      <GameHandle
        handle="ABC123"
        isPasswordProtected={true}
        copyLabel="Copy game code"
        copiedLabel="Copied!"
        passwordProtectedLabel="Password protected game"
      />,
    );

    expect(screen.getByText("Password protected game")).toBeInTheDocument();
  });

  it("render_whenNotPasswordProtected_doesNotShowLockLabel", () => {
    render(
      <GameHandle
        handle="ABC123"
        isPasswordProtected={false}
        copyLabel="Copy game code"
        copiedLabel="Copied!"
        passwordProtectedLabel="Password protected game"
      />,
    );

    expect(screen.queryByText("Password protected game")).not.toBeInTheDocument();
  });

  it("render_showsCopyButtonWithAccessibleName", () => {
    render(
      <GameHandle
        handle="ABC123"
        isPasswordProtected={false}
        copyLabel="Copy game code"
        copiedLabel="Copied!"
        passwordProtectedLabel="Password protected game"
      />,
    );

    expect(screen.getByRole("button", { name: "Copy game code" })).toBeInTheDocument();
  });

  it("copyButton_whenClicked_copiesHandleToClipboard", () => {
    render(
      <GameHandle
        handle="ABC123"
        isPasswordProtected={false}
        copyLabel="Copy game code"
        copiedLabel="Copied!"
        passwordProtectedLabel="Password protected game"
      />,
    );

    fireEvent.click(screen.getByRole("button", { name: "Copy game code" }));

    expect(navigator.clipboard.writeText).toHaveBeenCalledWith("ABC123");
  });

  it("copyButton_whenClicked_showsCopiedFeedback", async () => {
    vi.useFakeTimers();

    render(
      <GameHandle
        handle="ABC123"
        isPasswordProtected={false}
        copyLabel="Copy game code"
        copiedLabel="Copied!"
        passwordProtectedLabel="Password protected game"
      />,
    );

    await act(async () => {
      fireEvent.click(screen.getByRole("button", { name: "Copy game code" }));
    });

    expect(screen.getByRole("button", { name: "Copied!" })).toBeInTheDocument();

    vi.runAllTimers();
    vi.useRealTimers();
  });

  it("copyButton_afterFeedbackTimeout_revertsToOriginalLabel", async () => {
    vi.useFakeTimers();

    render(
      <GameHandle
        handle="ABC123"
        isPasswordProtected={false}
        copyLabel="Copy game code"
        copiedLabel="Copied!"
        passwordProtectedLabel="Password protected game"
      />,
    );

    await act(async () => {
      fireEvent.click(screen.getByRole("button", { name: "Copy game code" }));
    });

    expect(screen.getByRole("button", { name: "Copied!" })).toBeInTheDocument();

    act(() => { vi.runAllTimers(); });

    expect(screen.getByRole("button", { name: "Copy game code" })).toBeInTheDocument();

    vi.useRealTimers();
  });
});
