## Architecture

This application currently uses a **monolithic architecture** with all functionality in a single class for simplicity and educational purposes.

**Current Design:**
- Single `ExpenseTrackerApp.java` class
- Direct JDBC database operations
- Console-based user interface

**Planned Refactoring** (see [Future Enhancements](docs/FUTURE_ENHANCEMENTS.md)):
- Model-View-Controller (MVC) architecture
- Separate DAO, Service, and Controller layers
- JavaFX or web-based UI
- PostgreSQL migration
- Unit testing with JUnit

See [Architecture Diagrams](docs/diagrams/architecture-diagram.md) for detailed views.

## Security Features

- **SQL Injection Prevention**: Uses PreparedStatements for all database queries
- **Input Validation**: Robust validation for all user inputs
- **Data Integrity**: Foreign key constraints ensure referential integrity
- **Error Handling**: Comprehensive try-catch blocks with user-friendly messages

## Testing

Currently manual testing. Future plans include:
- JUnit 5 unit tests
- Integration tests with H2 in-memory database
- Test coverage with JaCoCo
- Automated testing in CI/CD pipeline

## Future Enhancements

**High Priority:**
- [ ] PostgreSQL migration for better scalability
- [ ] JavaFX GUI with charts and graphs
- [ ] Budget management and alerts
- [ ] Recurring transaction support

**Medium Priority:**
- [ ] Machine learning for expense categorization
- [ ] Receipt scanning and OCR
- [ ] Multi-currency support
- [ ] REST API development

**Low Priority:**
- [ ] Mobile application (Android/iOS)
- [ ] Cloud synchronization
- [ ] Bank integration via APIs

See complete roadmap in [FUTURE_ENHANCEMENTS.md](docs/FUTURE_ENHANCEMENTS.md)

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- SQLite for the lightweight database engine
- Java community for excellent documentation
- [Any other resources or inspirations]

---

⭐ **If you find this project helpful, please consider giving it a star! This project is a bit messy and still a work in progress, so please have patience!** ⭐