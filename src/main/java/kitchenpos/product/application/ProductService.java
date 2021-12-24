package kitchenpos.product.application;

import kitchenpos.product.domain.Product;
import kitchenpos.product.domain.ProductRepository;
import kitchenpos.product.dto.ProductRequest;
import kitchenpos.product.dto.ProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Transactional(readOnly = true)
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(final ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse create(final ProductRequest request) {
        Product product = request.toProduct();

        Product persistProduct = productRepository.save(product);
        return ProductResponse.of(persistProduct);
    }

    public List<ProductResponse> list() {
        List<Product> persistProducts = productRepository.findAll();

        return ProductResponse.fromList(persistProducts);
    }

    public Product findById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(NoSuchElementException::new);
    }

    public List<Product> findByIdIn(List<Long> productIds) {
        return productRepository.findByIdIn(productIds);
    }
}
