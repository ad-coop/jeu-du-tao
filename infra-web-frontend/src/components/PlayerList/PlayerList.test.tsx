import { fireEvent, render, screen } from "@testing-library/react";
import { PlayerList } from "./PlayerList";

const players = [
  { id: "p1", name: "Alice", role: "guardian" },
  { id: "p2", name: "Bob", role: "player" },
  { id: "p3", name: "Carol", role: "player" },
];

describe("PlayerList", () => {
  it("render_showsAllPlayerNames", () => {
    render(
      <PlayerList
        players={players}
        currentPlayerId="p1"
        isGuardian={false}
        kickLabel={(name) => `Expulser ${name}`}
      />,
    );

    expect(screen.getByText("Alice")).toBeInTheDocument();
    expect(screen.getByText("Bob")).toBeInTheDocument();
    expect(screen.getByText("Carol")).toBeInTheDocument();
  });

  it("render_showsGuardianBadgeForGuardianRole", () => {
    render(
      <PlayerList
        players={players}
        currentPlayerId="p2"
        isGuardian={false}
        kickLabel={(name) => `Expulser ${name}`}
      />,
    );

    expect(screen.getByText("Gardien")).toBeInTheDocument();
  });

  it("render_hasAriaLivePolite", () => {
    render(
      <PlayerList
        players={players}
        currentPlayerId="p1"
        isGuardian={false}
        kickLabel={(name) => `Expulser ${name}`}
      />,
    );

    const list = screen.getByRole("list");
    expect(list).toHaveAttribute("aria-live", "polite");
  });

  it("render_whenIsGuardian_showsKickButtonsForOtherPlayers", () => {
    const handleKick = vi.fn();
    render(
      <PlayerList
        players={players}
        currentPlayerId="p1"
        isGuardian={true}
        onKick={handleKick}
        kickLabel={(name) => `Expulser ${name}`}
      />,
    );

    expect(screen.getByRole("button", { name: "Expulser Bob" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Expulser Carol" })).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Expulser Alice" })).not.toBeInTheDocument();
  });

  it("render_whenNotGuardian_doesNotShowKickButtons", () => {
    render(
      <PlayerList
        players={players}
        currentPlayerId="p2"
        isGuardian={false}
        kickLabel={(name) => `Expulser ${name}`}
      />,
    );

    expect(screen.queryByRole("button")).not.toBeInTheDocument();
  });

  it("render_whenGuardianIsNotFirst_showsGuardianAtTop", () => {
    const playersWithLateGuardian = [
      { id: "p1", name: "Bob", role: "player" },
      { id: "p2", name: "Carol", role: "player" },
      { id: "p3", name: "Alice", role: "guardian" },
    ];

    render(
      <PlayerList
        players={playersWithLateGuardian}
        currentPlayerId="p1"
        isGuardian={false}
        kickLabel={(name) => `Expulser ${name}`}
      />,
    );

    const items = screen.getAllByRole("listitem");
    expect(items[0]).toHaveTextContent("Alice");
    expect(items[1]).toHaveTextContent("Bob");
    expect(items[2]).toHaveTextContent("Carol");
  });

  it("render_whenMultiplePlayers_preservesArrivalOrderAfterGuardian", () => {
    const playersInOrder = [
      { id: "p1", name: "Bob", role: "player" },
      { id: "p2", name: "Carol", role: "player" },
      { id: "p3", name: "Alice", role: "guardian" },
      { id: "p4", name: "Dave", role: "player" },
    ];

    render(
      <PlayerList
        players={playersInOrder}
        currentPlayerId="p1"
        isGuardian={false}
        kickLabel={(name) => `Expulser ${name}`}
      />,
    );

    const items = screen.getAllByRole("listitem");
    expect(items[0]).toHaveTextContent("Alice");
    expect(items[1]).toHaveTextContent("Bob");
    expect(items[2]).toHaveTextContent("Carol");
    expect(items[3]).toHaveTextContent("Dave");
  });

  it("kickButton_whenClicked_callsOnKickWithPlayerId", () => {
    const handleKick = vi.fn();
    render(
      <PlayerList
        players={players}
        currentPlayerId="p1"
        isGuardian={true}
        onKick={handleKick}
        kickLabel={(name) => `Expulser ${name}`}
      />,
    );

    fireEvent.click(screen.getByRole("button", { name: "Expulser Bob" }));

    expect(handleKick).toHaveBeenCalledWith("p2");
  });
});
