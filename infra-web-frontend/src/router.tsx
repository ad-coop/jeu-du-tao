import { createBrowserRouter } from "react-router";
import { LandingPage } from "./pages/LandingPage/LandingPage";
import { CreateGamePage } from "./pages/CreateGamePage/CreateGamePage";
import { JoinGamePage } from "./pages/JoinGamePage/JoinGamePage";
import { GameHandleRouter } from "./pages/GameHandleRouter/GameHandleRouter";
import { ErrorPage } from "./pages/ErrorPage/ErrorPage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <LandingPage />,
  },
  {
    path: "/game/create",
    element: <CreateGamePage />,
  },
  {
    path: "/game/join",
    element: <JoinGamePage />,
  },
  {
    path: "/game/:handle",
    element: <GameHandleRouter />,
  },
  {
    path: "/error/:errorType",
    element: <ErrorPage />,
  },
  {
    path: "*",
    element: <ErrorPage errorType="not-found" />,
  },
]);
