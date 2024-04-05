import { decodeEnv, buildTrustiEnv } from "@trusti-ui/common";

export const ENV = buildTrustiEnv(decodeEnv(window._env));

export default ENV;
