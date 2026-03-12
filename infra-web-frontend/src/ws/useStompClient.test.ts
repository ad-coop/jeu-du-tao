import { act, renderHook } from "@testing-library/react";
import { useStompClient } from "./useStompClient";

const mockActivate = vi.fn();
const mockDeactivate = vi.fn().mockResolvedValue(undefined);
const mockSubscribe = vi.fn();
let capturedOnConnect: (() => void) | undefined;

vi.mock("@stomp/stompjs", () => ({
  Client: vi.fn().mockImplementation(function(options: {
    onConnect?: () => void;
  }) {
    capturedOnConnect = options.onConnect;
    return {
      activate: mockActivate,
      deactivate: mockDeactivate,
      subscribe: mockSubscribe,
    };
  }),
}));

describe("useStompClient", () => {
  beforeEach(() => {
    mockActivate.mockClear();
    mockDeactivate.mockClear();
    mockSubscribe.mockClear();
    capturedOnConnect = undefined;
  });

  it("mount_activatesStompClient", () => {
    renderHook(() =>
      useStompClient({
        handle: "abc123",
        playerId: "p1",
        onPlayersUpdate: vi.fn(),
        onKicked: vi.fn(),
      }),
    );

    expect(mockActivate).toHaveBeenCalledTimes(1);
  });

  it("unmount_deactivatesStompClient", () => {
    const { unmount } = renderHook(() =>
      useStompClient({
        handle: "abc123",
        playerId: "p1",
        onPlayersUpdate: vi.fn(),
        onKicked: vi.fn(),
      }),
    );

    unmount();

    expect(mockDeactivate).toHaveBeenCalledTimes(1);
  });

  it("initial_isConnectedIsFalse", () => {
    const { result } = renderHook(() =>
      useStompClient({
        handle: "abc123",
        playerId: "p1",
        onPlayersUpdate: vi.fn(),
        onKicked: vi.fn(),
      }),
    );

    expect(result.current.isConnected).toBe(false);
  });

  it("onConnect_isConnectedBecomesTrue", () => {
    const { result } = renderHook(() =>
      useStompClient({
        handle: "abc123",
        playerId: "p1",
        onPlayersUpdate: vi.fn(),
        onKicked: vi.fn(),
      }),
    );

    // Simulate STOMP connection established — must wrap in act() since it triggers setState
    act(() => {
      if (capturedOnConnect) capturedOnConnect();
    });

    expect(result.current.isConnected).toBe(true);
  });
});
