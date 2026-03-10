import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router";
import { HeroSection } from "./HeroSection";

vi.mock("../../i18n", () => ({
  useTranslation: () => ({ t: (key: string) => key }),
}));

describe("HeroSection", () => {
  it("render_showsBannerWithLogoHeadingAndParagraphs", () => {
    render(
      <MemoryRouter>
        <HeroSection />
      </MemoryRouter>,
    );

    expect(screen.getByRole("banner")).toBeInTheDocument();
    expect(screen.getByRole("heading", { level: 1 })).toBeInTheDocument();
    expect(screen.getByRole("img")).toHaveAttribute("alt");
    expect(screen.getByText("landing.subtitle")).toBeInTheDocument();
    expect(screen.getByText("landing.intro")).toBeInTheDocument();
  });

  it("render_showsTwoCtaLinks", () => {
    render(
      <MemoryRouter>
        <HeroSection />
      </MemoryRouter>,
    );

    expect(screen.getAllByRole("link")).toHaveLength(2);
  });

  it("render_createGameLinkPointsToCreateRoute", () => {
    render(
      <MemoryRouter>
        <HeroSection />
      </MemoryRouter>,
    );

    const createLink = screen.getByText("landing.cta.createGame");
    expect(createLink).toHaveAttribute("href", "/game/create");
  });

  it("render_joinGameLinkPointsToJoinRoute", () => {
    render(
      <MemoryRouter>
        <HeroSection />
      </MemoryRouter>,
    );

    const joinLink = screen.getByText("landing.cta.joinGame");
    expect(joinLink).toHaveAttribute("href", "/game/join");
  });
});
