import { render, screen } from "@testing-library/react";
import App from "./App";

describe("App", () => {
  it("render_showsMainHeading", () => {
    render(<App />);
    expect(screen.getByRole("heading", { level: 1 })).toBeInTheDocument();
  });
});
