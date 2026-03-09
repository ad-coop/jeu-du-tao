import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router";
import { ErrorPage } from "./ErrorPage";

vi.mock("../../i18n", () => ({
  useTranslation: () => ({ t: (key: string) => key }),
}));

function renderWithRoute(errorType?: string) {
  if (errorType) {
    return render(
      <MemoryRouter initialEntries={[`/error/${errorType}`]}>
        <Routes>
          <Route path="/error/:errorType" element={<ErrorPage />} />
        </Routes>
      </MemoryRouter>,
    );
  }
  return render(
    <MemoryRouter>
      <ErrorPage errorType="not-found" />
    </MemoryRouter>,
  );
}

describe("ErrorPage", () => {
  it("render_whenNotFoundParam_showsNotFoundMessage", () => {
    renderWithRoute("not-found");

    expect(screen.getByText("game.error.notFound")).toBeInTheDocument();
  });

  it("render_whenAlreadyStartedParam_showsAlreadyStartedMessage", () => {
    renderWithRoute("already-started");

    expect(screen.getByText("game.error.alreadyStarted")).toBeInTheDocument();
  });

  it("render_whenKickedParam_showsKickedMessage", () => {
    renderWithRoute("kicked");

    expect(screen.getByText("game.error.kicked")).toBeInTheDocument();
  });

  it("render_whenPropErrorType_showsMessage", () => {
    renderWithRoute();

    expect(screen.getByText("game.error.notFound")).toBeInTheDocument();
  });

  it("render_showsHomeLinkToRoot", () => {
    renderWithRoute("not-found");

    const homeLink = screen.getByRole("link", { name: "game.error.home" });
    expect(homeLink).toHaveAttribute("href", "/");
  });

  it("render_whenUnknownErrorType_fallsBackToNotFound", () => {
    renderWithRoute("unknown-type");

    expect(screen.getByText("game.error.notFound")).toBeInTheDocument();
  });
});
