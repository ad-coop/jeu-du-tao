import { render, screen } from "@testing-library/react";
import { createMemoryRouter, RouterProvider } from "react-router";
import { GameSessionProvider } from "./hooks/useGameSession";

vi.mock("./i18n", () => ({
  useTranslation: () => ({ t: (key: string) => key }),
}));

vi.mock("./components/Toast", () => ({
  useToast: () => ({ showToast: vi.fn() }),
}));

describe("App", () => {
  it("render_showsMainHeading", () => {
    const testRouter = createMemoryRouter(
      [
        {
          path: "/",
          element: (
            <div>
              <h1>Test heading</h1>
            </div>
          ),
        },
      ],
      { initialEntries: ["/"] },
    );

    render(
      <GameSessionProvider>
        <RouterProvider router={testRouter} />
      </GameSessionProvider>,
    );

    expect(screen.getByRole("heading", { level: 1 })).toBeInTheDocument();
  });
});
