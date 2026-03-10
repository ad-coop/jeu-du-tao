import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router";
import { LandingPage } from "./LandingPage";

vi.mock("../../i18n", () => ({
  useTranslation: () => ({ t: (key: string) => key }),
}));

describe("LandingPage", () => {
  it("render_showsHeroAndFooter", () => {
    render(
      <MemoryRouter>
        <LandingPage />
      </MemoryRouter>,
    );

    expect(screen.getByRole("banner")).toBeInTheDocument();
    expect(screen.getByRole("contentinfo")).toBeInTheDocument();
  });
});
