# Real-Time Price Tracker

An Android application for tracking stock prices in real-time using WebSocket connections.

---

## Demo

<!-- Attach your demo videos here. You can either: -->
<!-- 1. Upload webm files to a demos/ folder and reference them -->
<!-- 2. Embed video links if hosting elsewhere -->
<!-- 3. Use GitHub's video upload feature in issues/PRs and link here -->

| Demo   | Description            | Video |
|--------|------------------------|-------|
| Demo 1 | Light Theme            |https://github.com/user-attachments/assets/a31d4776-21a5-4442-b8c5-1960b642038f|
| Demo 2 | Dark Theme             |https://github.com/user-attachments/assets/22bacf66-83df-4eed-a48b-982b3ac0d968|
| Demo 3 | Process Death Scenario |https://github.com/user-attachments/assets/735e856c-34dc-4c12-93b8-0da27b999e80|

## Package Structure Rationale
> The package structure tries to adhere to Clean Architecture principles, so that each of the layer is just aware of the layer below it.
> This will help to scale, maintain, test and make sense of the codebase easier.
>
> ### feature package ###
> - All the feature related logic goes here. In case we want to modularize the repo in future, this package can be easily extracted into its own gradle module.
> - feature package contains 4 packages : data, domain, presentation and di
> - Dependency is like this: presentation -> domain -> data
> - di package is cross cutting, basically all the gluing logic of different components goes here. This package will not contain any business logic
>
> 
> ### core package ###
> - This package will contain all the cross cutting components, any of the layers can consume it depending on the need.
> - This houses classes like Analytics Service, Logging Service
> - In case we want to modularize the repo in future, this package can be easily extracted into its own gradle module.
> 
> 
> ### ui package ###
> - This package will contain everything related to UI - theming, common components.
> - In case we want to modularize the repo in future, this package can be easily extracted into its own gradle module.


---

## Rationale behind different classes
> ### Why StockPriceDetailsUseCase?
> - This will host pure business logic, doesn't host any state nor uses any Android Specific dependencies.
> - With Android 16/ Android 17 introducing AppFunctions, our apps can become headless app which just route the requests from Gemini to our Backend servers, so creating a class which holds pure business logic becomes more meaningful now
> 
>
> ### Why StockPriceDetailsRepository?
> - This class depends on an interface StockPriceDetailsDataSource
> - In future if the socket based implementation needs to be switched with long polling then we can just provide another implementation of StockPriceDetailsDataSource without impacting StockPriceDetailsRepository
> - In future if there is a need to support offline rendering of data, this class can handle it without impacting the business logic layer

---

## Some highlights ##
> - The presentation layer uses MVVM architecture
> - The project uses Hilt for Dependency Injection
> - The StockPriceDetailScreen can maintain state across configuration changes and Process Death scenarios as it uses SavedStateHandle
> - For handling socket connections OKhttp library is being used
> - The app supports both Light and Dark Theme, and also dynamic theme for Android 12+ devices

