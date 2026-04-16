{
  description = "Guess game";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
  };

  outputs = {self, nixpkgs}:
    let
      system = "x86_64-linux";
      pkgs = nixpkgs.legacyPackages.${system};
    in
    {
      packages.${system}.default = pkgs.maven.buildMavenPackage {
        pname = "result";
        version = "0.1.0";
        src = ./.;

        jdk = pkgs.jdk8;
        mvnHash = "sha256-ybaqpMdF6GcmVvybg6kc5z7QOB7EBrpxqrLGehg3fy4=";

        installPhase = ''
          mkdir -p $out/share/java
          cp gg-client/target/*.jar $out/share/java
          cp gg-server/target/*.jar $out/share/java
        '';

      };

      devShells.${system}.default = pkgs.mkShell {
        buildInputs = [
          pkgs.jdk8
          pkgs.maven
        ];
        shellHook = ''
          export JAVA_HOME=${pkgs.jdk21}/lib/openjdk
	  alias run-client='mvn compile exec:java -Dexec.mainClass="com.guessgame.client.App"'
          echo "Java 21 loaded"
          java -version
        '';
      };
    };
}
