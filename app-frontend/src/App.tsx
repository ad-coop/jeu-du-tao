import { RouterProvider } from "react-router";
import { router } from "./router";
import { GameSessionProvider } from "./hooks/useGameSession";

function App() {
  return (
    <GameSessionProvider>
      <RouterProvider router={router} />
    </GameSessionProvider>
  );
}

export default App;
