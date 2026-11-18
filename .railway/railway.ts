import { defineRailway, github, project, service } from "railway/iac";

export const partial = "bytebooks-api";

export default defineRailway(() => {
  const api = service("bytebooks-api", {
    source: github("lautaromoreyraa/bytebooks-api", { branch: "main", checkSuites: false }),
    build: { builder: "DOCKERFILE", dockerfilePath: "Dockerfile" },
    healthcheck: "/actuator/health",
    healthcheckTimeout: 180,
    deploy: {
      restartPolicyMaxRetries: 3,
      sleepApplication: true,
      limitOverride: { containers: { cpu: 0.5, memoryBytes: 500000000 } },
    },
  });
  return project("demos", {
    variables: { managed: false },
    resources: [api],
  });
});
