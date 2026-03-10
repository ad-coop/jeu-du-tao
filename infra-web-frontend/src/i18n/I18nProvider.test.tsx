import { render, screen, waitFor } from "@testing-library/react";
import { I18nProvider } from "./I18nProvider";
import { useTranslation } from "./useTranslation";

function TestComponent({ translationKey }: { translationKey: string }) {
  const { t } = useTranslation();
  return <div>{t(translationKey)}</div>;
}

function TestComponentWithParams({
  translationKey,
  params,
}: {
  translationKey: string;
  params: Record<string, string>;
}) {
  const { t } = useTranslation();
  return <div>{t(translationKey, params)}</div>;
}

describe("I18nProvider", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("t_whenLoaded_returnsTranslatedText", async () => {
    vi.mocked(fetch).mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ "landing.title": "Le Jeu du Tao" }),
    } as Response);

    render(
      <I18nProvider>
        <TestComponent translationKey="landing.title" />
      </I18nProvider>,
    );

    await waitFor(() => {
      expect(screen.getByText("Le Jeu du Tao")).toBeInTheDocument();
    });
  });

  it("t_whenParamsProvided_interpolatesValues", async () => {
    vi.mocked(fetch).mockResolvedValue({
      ok: true,
      json: () =>
        Promise.resolve({ "game.welcome": "Bienvenue, {name} !" }),
    } as Response);

    render(
      <I18nProvider>
        <TestComponentWithParams
          translationKey="game.welcome"
          params={{ name: "Alice" }}
        />
      </I18nProvider>,
    );

    await waitFor(() => {
      expect(screen.getByText("Bienvenue, Alice !")).toBeInTheDocument();
    });
  });

  it("t_whenFetchFails_returnsKeyAndLogsWarning", async () => {
    vi.mocked(fetch).mockRejectedValue(new Error("Network error"));
    const warnSpy = vi.spyOn(console, "warn").mockImplementation(() => {});

    render(
      <I18nProvider>
        <TestComponent translationKey="landing.title" />
      </I18nProvider>,
    );

    await waitFor(() => {
      expect(warnSpy).toHaveBeenCalled();
    });
    expect(screen.getByText("landing.title")).toBeInTheDocument();

    warnSpy.mockRestore();
  });
});
