import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router";
import { CreateGamePage } from "./CreateGamePage";
import { gameApi } from "../../api/gameApi";
import type { GameSession } from "../../hooks/useGameSession";

const mockNavigate = vi.fn();
const mockSetSession = vi.fn();

vi.mock("../../i18n", () => ({
  useTranslation: () => ({ t: (key: string) => key }),
}));

vi.mock("react-router", async (importOriginal) => {
  const actual = await importOriginal<typeof import("react-router")>();
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

vi.mock("../../api/gameApi", () => ({
  gameApi: {
    createGame: vi.fn(),
  },
  ApiError: class ApiError extends Error {
    status: number;
    code: string;
    constructor(status: number, code: string, message: string) {
      super(message);
      this.status = status;
      this.code = code;
    }
  },
}));

vi.mock("../../hooks/useGameSession", () => ({
  useGameSession: () => ({ session: null, setSession: mockSetSession }),
  GameSessionProvider: ({ children }: { children: React.ReactNode }) => children,
}));

function renderPage() {
  return render(
    <MemoryRouter>
      <CreateGamePage />
    </MemoryRouter>,
  );
}

describe("CreateGamePage", () => {
  beforeEach(() => {
    mockNavigate.mockClear();
    mockSetSession.mockClear();
    vi.mocked(gameApi.createGame).mockClear();
  });

  it("render_showsCreateGameForm", () => {
    renderPage();

    expect(screen.getByRole("main")).toBeInTheDocument();
    expect(screen.getByLabelText(/game.create.userName.label/)).toBeInTheDocument();
    expect(screen.getByLabelText(/game.create.email.label/)).toBeInTheDocument();
    expect(screen.getByLabelText(/game.create.password.label/)).toBeInTheDocument();
  });

  it("submit_whenUserNameEmpty_showsValidationError", async () => {
    renderPage();

    fireEvent.click(screen.getByRole("button", { name: /game.create.submit/ }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toBeInTheDocument();
    });
    expect(gameApi.createGame).not.toHaveBeenCalled();
  });

  it("submit_whenUserNameTooLong_showsValidationError", async () => {
    renderPage();

    fireEvent.change(screen.getByLabelText(/game.create.userName.label/), {
      target: { value: "a".repeat(51) },
    });
    fireEvent.click(screen.getByRole("button", { name: /game.create.submit/ }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toBeInTheDocument();
    });
    expect(gameApi.createGame).not.toHaveBeenCalled();
  });

  it("submit_whenEmailInvalid_showsValidationError", async () => {
    renderPage();

    fireEvent.change(screen.getByLabelText(/game.create.userName.label/), {
      target: { value: "Alice" },
    });
    fireEvent.change(screen.getByLabelText(/game.create.email.label/), {
      target: { value: "not-an-email" },
    });
    fireEvent.click(screen.getByRole("button", { name: /game.create.submit/ }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toBeInTheDocument();
    });
    expect(gameApi.createGame).not.toHaveBeenCalled();
  });

  it("submit_whenValid_callsApiAndNavigates", async () => {
    vi.mocked(gameApi.createGame).mockResolvedValue({
      handle: "game-abc",
      playerId: "player-1",
      passwordProtected: false,
      hasEmail: true,
    });

    renderPage();

    fireEvent.change(screen.getByLabelText(/game.create.userName.label/), {
      target: { value: "Alice" },
    });
    fireEvent.click(screen.getByRole("button", { name: /game.create.submit/ }));

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith("/game/game-abc");
    });

    expect(gameApi.createGame).toHaveBeenCalledWith({
      userName: "Alice",
      email: undefined,
      password: undefined,
    });

    const expectedSession: GameSession = {
      handle: "game-abc",
      playerId: "player-1",
      role: "guardian",
      isPasswordProtected: false,
      hasEmail: true,
    };
    expect(mockSetSession).toHaveBeenCalledWith(expectedSession);
  });

  it("submit_whenApiError_showsErrorMessage", async () => {
    vi.mocked(gameApi.createGame).mockRejectedValue(
      new Error("Server error"),
    );

    renderPage();

    fireEvent.change(screen.getByLabelText(/game.create.userName.label/), {
      target: { value: "Alice" },
    });
    fireEvent.click(screen.getByRole("button", { name: /game.create.submit/ }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toBeInTheDocument();
    });
    expect(mockNavigate).not.toHaveBeenCalled();
  });
});
