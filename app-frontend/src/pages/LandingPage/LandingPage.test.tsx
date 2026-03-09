import { render, screen } from "@testing-library/react";
import { LandingPage } from "./LandingPage";

vi.mock("../../i18n", () => ({
  useTranslation: () => ({ t: (key: string) => key }),
}));

vi.mock("../../components/Toast", () => ({
  useToast: () => ({ showToast: vi.fn() }),
}));

describe("LandingPage", () => {
  it("render_showsHeroAndFooter", () => {
    render(<LandingPage />);

    expect(screen.getByRole("banner")).toBeInTheDocument();
    expect(screen.getByRole("contentinfo")).toBeInTheDocument();
  });
});
