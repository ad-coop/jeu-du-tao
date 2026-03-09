import { fireEvent, render, screen } from "@testing-library/react";
import { HeroSection } from "./HeroSection";

const mockShowToast = vi.fn();

vi.mock("../../i18n", () => ({
  useTranslation: () => ({ t: (key: string) => key }),
}));

vi.mock("../Toast", () => ({
  useToast: () => ({ showToast: mockShowToast }),
}));

describe("HeroSection", () => {
  beforeEach(() => {
    mockShowToast.mockClear();
  });

  it("render_showsBannerWithLogoHeadingAndParagraphs", () => {
    render(<HeroSection />);

    expect(screen.getByRole("banner")).toBeInTheDocument();
    expect(screen.getByRole("heading", { level: 1 })).toBeInTheDocument();
    expect(screen.getByRole("img")).toHaveAttribute("alt");
    expect(screen.getByText("landing.subtitle")).toBeInTheDocument();
    expect(screen.getByText("landing.intro")).toBeInTheDocument();
  });

  it("render_showsTwoCtaButtons", () => {
    render(<HeroSection />);

    expect(screen.getAllByRole("button")).toHaveLength(2);
  });

  it("ctaClick_triggersToast", () => {
    render(<HeroSection />);

    const [firstButton] = screen.getAllByRole("button");
    if (!firstButton) throw new Error("No buttons found");
    fireEvent.click(firstButton);

    expect(mockShowToast).toHaveBeenCalledWith("landing.toast.comingSoon");
  });
});
