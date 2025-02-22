/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

import AwsCommonRuntimeKit

public struct Headers: Equatable {
    public var headers: [Header] = []

    /// Creates an empty instance.
    public init() {}

    /// Creates an instance from a `[String: String]`. Duplicate case-insensitive names are collapsed into the last name
    /// and value encountered.
    public init(_ dictionary: [String: String]) {
        self.init()

        dictionary.forEach { add(name: $0.key, value: $0.value)}
    }
    
    /// Creates an instance from a `[String: [String]]`. 
    public init(_ dictionary: [String: [String]]) {
        self.init()

        dictionary.forEach { key, values in add(name: key, values: values) }
    }

    /// Case-insensitively updates or appends a `Header` into the instance using the provided `name` and `value`.
    ///
    /// - Parameters:
    ///   - name:  The `String` name.
    ///   - value: The `String` value.
    public mutating func add(name: String, value: String) {
        let header = Header(name: name, value: value)
        add(header)
    }
    
    /// Case-insensitively updates the value of a `Header` by appending the new values to it or appends a `Header`
    /// into the instance using the provided `name` and `values`.
    ///
    /// - Parameters:
    ///   - name:  The `String` name.
    ///   - values: The `[String]` values.
    public mutating func add(name: String, values: [String]) {
        let header = Header(name: name, values: values)
        add(header)
    }
    
    /// Case-insensitively updates the value of a `Header` by appending the new values to it or appends a `Header`
    /// into the instance using the provided `Header`.
    ///
    /// - Parameters:
    ///   - header:  The `Header` to be added or updated.
    public mutating func add(_ header: Header) {
        guard let index = headers.index(of: header.name) else {
            headers.append(header)
            return
        }
        headers[index].value.append(contentsOf: header.value)
    }
    
    /// Case-insensitively updates the value of a `Header` by replacing the values of it or appends a `Header`
    /// into the instance if it does not exist using the provided `Header`.
    ///
    /// - Parameters:
    ///   - header:  The `Header` to be added or updated.
    public mutating func update(_ header: Header) {
        guard let index = headers.index(of: header.name) else {
            headers.append(header)
            return
        }
        headers.replaceSubrange(index...index, with: [header])
    }
    
    /// Case-insensitively updates the value of a `Header` by replacing the values of it or appends a `Header`
    /// into the instance if it does not exist using the provided `Header`.
    ///
    /// - Parameters:
    ///   - header:  The `Header` to be added or updated.
    public mutating func update(name: String, value: [String]) {
        let header = Header(name: name, values: value)
        update(header)
    }
    
    /// Case-insensitively updates the value of a `Header` by replacing the values of it or appends a `Header`
    /// into the instance if it does not exist using the provided `Header`.
    ///
    /// - Parameters:
    ///   - header:  The `Header` to be added or updated.
    public mutating func update(name: String, value: String) {
        let header = Header(name: name, value: value)
        update(header)
    }
    
    /// Case-insensitively adds all `Headers` into the instance using the provided `[Headers]` array.
    ///
    /// - Parameters:
    ///   - headers:  The `Headers` object.
    public mutating func addAll(headers: Headers) {
        self.headers.append(contentsOf: headers.headers)
    }

    /// Case-insensitively removes a `Header`, if it exists, from the instance.
    ///
    /// - Parameter name: The name of the `HTTPHeader` to remove.
    public mutating func remove(name: String) {
        guard let index = headers.index(of: name) else { return }

        headers.remove(at: index)
    }
    
    /// Case-insensitively find a header's values by name.
    ///
    /// - Parameter name: The name of the header to search for, case-insensitively.
    ///
    /// - Returns: The values of the header, if they exist.
    public func values(for name: String) -> [String]? {
        guard let indices = headers.indices(of: name), !indices.isEmpty else { return nil }
        var values = [String]()
        for index in indices {
            values.append(contentsOf: headers[index].value)
        }
        
        return values
    }
    
    /// Case-insensitively find a header's value by name.
    ///
    /// - Parameter name: The name of the header to search for, case-insensitively.
    ///
    /// - Returns: The value of header as a comma delimited string, if it exists.
    public func value(for name: String) -> String? {
        guard let values = values(for: name) else {
            return nil
        }
        return values.joined(separator: ",")
    }
    
    public func exists(name: String) -> Bool {
        guard headers.index(of: name) != nil else {
            return false
        }
        
        guard let value = value(for: name) else {
            return false
        }
        
        return !value.isEmpty
    }

    /// The dictionary representation of all headers.
    ///
    /// This representation does not preserve the current order of the instance.
    public var dictionary: [String: [String]] {
        let namesAndValues = headers.map { ($0.name, $0.value) }

        return Dictionary(namesAndValues) { (first, last) -> [String] in
            return first + last
        }
    }
}

extension Array where Element == Header {
    /// Case-insensitively finds the index of an `Header` with the provided name, if it exists.
    func index(of name: String) -> Int? {
        let lowercasedName = name.lowercased()
        return firstIndex { $0.name.lowercased() == lowercasedName }
    }
    
    /// Case-insensitively finds the indexes of an `Header` with the provided name, if it exists.
    func indices(of name: String) -> [Int]? {
        let lowercasedName = name.lowercased()
        return enumerated().compactMap { $0.element.name.lowercased() == lowercasedName ? $0.offset : nil }
    }
}

public struct Header: Equatable {
    public var name: String
    public var value: [String]

    public init(name: String, values: [String]) {
        self.name = name
        self.value = values
    }
    
    public init(name: String, value: String) {
        self.name = name
        self.value = [value]
    }
}

extension Headers {
    func toHttpHeaders() -> HttpHeaders {
        let httpHeaders = HttpHeaders()
        
        for header in headers {
            _ = httpHeaders.add(name: header.name, value: header.value.joined(separator: ","))
        }
        return httpHeaders
    }
    
    init(httpHeaders: HttpHeaders) {
        self.init()
        let headers = httpHeaders.getAll()
        headers.forEach { (header) in
            add(name: header.name, value: header.value)
        }
    }
    
    public mutating func addAll(httpHeaders: HttpHeaders) {
        let headers = httpHeaders.getAll()
        headers.forEach { (header) in
            add(name: header.name, value: header.value)
        }
    }
}

extension Headers: CustomDebugStringConvertible {
    public var debugDescription: String {
        return dictionary.map {"\($0.key): \($0.value.joined(separator: ", "))"}.joined(separator: ", \n")
    }
}
