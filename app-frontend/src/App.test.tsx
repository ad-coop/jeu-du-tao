import { render, screen } from "@testing-library/react";
import App from "./App";

vi.mock("./i18n", () => ({
  useTranslation: () => ({ t: (key: string) => key }),
}));

vi.mock("./components/Toast", () => ({
  useToast: () => ({ showToast: vi.fn() }),
}));

describe("App", () => {
  it("render_showsMainHeading", () => {
    render(<App />);

    expect(screen.getByRole("heading", { level: 1 })).toBeInTheDocument();
  });
});
