# 잔해

멸망한 연구소 도시의 Paper 26.3 RPG 플러그인입니다. 직업을 고르고, 생명체를 기록하고, 상점·대장간·제작소·창고·감정소·전당포·암시장·도감·요리로 버팁니다.

만든 파일은 `output`에 있다. `janhae-1.0.0.jar`, `janhae-pack.zip`, 로컬 서버에서 확인할 순서가 적힌 `로컬-테스트.md`다.

## 서버에 넣기

Paper 26.3과 Java 25가 필요합니다.

1. `gradle build` 로 `build/libs/janhae-1.0.0.jar` 를 만듭니다.
2. 그 jar를 서버의 `plugins` 폴더에 넣습니다.
3. 서버를 켜면 `plugins/Janhae/config.yml` 이 생깁니다.
4. `resource-pack.public-base-url` 에 플레이어가 닿는 주소를 적습니다. 예: `http://서버IP:8765`
5. 서버를 다시 켜면, 접속할 때 리소스팩을 받겠냐고 물어봅니다. 받아야 무기와 갑옷 모습이 보입니다.

플러그인이 `8765` 포트로 팩 파일만 열어 둡니다. 그 포트가 방화벽 밖으로 열려 있어야 합니다.

## 플레이

처음 들어오면 무적과 실명 상태에서 직업을 고릅니다.

- 연구원은 조사와 첫 발견, 연구기록에서 경험치를 가장 많이 받습니다. 채집 키트를 오른클릭하면 조사가 됩니다.
- 생존가는 체력이 8칸이고, 처음 보는 생명체에게 더 세게 칩니다. 체력이 30% 아래면 잠깐 피해가 줄어들고, 음식을 더 빨리 먹습니다.
- 요리사는 `/요리` 에서 부산물 8개로 레시피를 열고, 그다음 2개로 음식을 만듭니다.

시설은 `/메뉴` 에 모여 있습니다. `/상점` `/대장간` `/제작소` `/창고` `/감정소` `/전당포` `/암시장` `/도감` `/요리`

창고는 90칸이고 `/창고 2` 가 둘째 페이지입니다. 암시장은 한국시간 자정마다 전설 3칸이 바뀌고, 사면 그날은 그 칸이 빕니다.

## 관리

오피만 쓸 수 있습니다.

```
/janhae admin job <플레이어> <researcher|survivor|cook>
/janhae admin reset <플레이어>
/janhae admin money <플레이어> <금액>
/janhae admin xp <플레이어> <경험치>
/janhae admin give <플레이어> <아이템ID> [개수]
/janhae admin reload
```

아이템 ID는 `rust_sword`, `sealed_sample`, `infected_tissue` 같은 영문입니다.

## 리소스팩을 다시 만들 때

```
python3 tools/build_pack.py
```

`resourcepack/` 과 `src/main/resources/janhae-pack.zip` 이 갱신됩니다. 다시 빌드해야 jar에 들어갑니다.
